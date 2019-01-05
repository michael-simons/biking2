/*
 * Copyright 2014-2019 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ac.simons.biking2.bikingpictures;

import ac.simons.biking2.config.DatastoreConfig;
import ac.simons.biking2.support.JAXBContextFactory;
import ac.simons.biking2.bikingpictures.rss.RSS;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Michael J. Simons, 2014-02-17
 */
@Component
@Profile({"default", "prod"})
@ConditionalOnBean(DailyFratzeProvider.class)
@Slf4j
class FetchBikingPicturesJob {

    private final DailyFratzeProvider dailyFratzeProvider;
    private final BikingPictureRepository bikingPictureRepository;
    private final File bikingPicturesStorage;
    private final JAXBContext rssContext;

    FetchBikingPicturesJob(
            final DailyFratzeProvider dailyFratzeProvider,
            final BikingPictureRepository bikingPictureRepository,
            final File datastoreBaseDirectory
    ) {
        this.dailyFratzeProvider = dailyFratzeProvider;
        this.bikingPictureRepository = bikingPictureRepository;

        this.bikingPicturesStorage = new File(datastoreBaseDirectory, DatastoreConfig.BIKING_PICTURES_DIRECTORY);
        if (!(this.bikingPicturesStorage.isDirectory() || this.bikingPicturesStorage.mkdirs())) {
            throw new BikingPicturesStorageException("Could not create bikingPicturesStorage!");
        }

        this.rssContext = new JAXBContextFactory(RSS.class).createContext();
    }

    @PostConstruct
    @Scheduled(cron = "${biking2.fetch-biking-picture-cron:0 0 */8 * * *}")
    public void run() {
        download(createDownloadList());
    }

    List<BikingPictureEntity> createDownloadList() {
        final List<BikingPictureEntity> rv = new ArrayList<>();

        // Current (or 1st) url
        String url = null;
        // Current rss data
        RSS rss;

        final ZonedDateTime maxPubDate = this.bikingPictureRepository.getMaxPubDate().toZonedDateTime();
        boolean foundOlderThanMaxPubDate = false;
        do {
            rss = getRSSFeed(url);
            if (rss == null) {
                log.warn("There was a problem getting the feed data");
            } else {
                final List<BikingPictureEntity> intermediateResult
                        = rss.getChannel()
                        .getItems()
                        .stream()
                        .filter(item -> item.getPubDate().isAfter(maxPubDate))
                        .map(item -> new BikingPictureEntity(item.getGuid().getValue(), item.getPubDate(), item.getLink()))
                        .collect(toList());
                rv.addAll(intermediateResult);
                foundOlderThanMaxPubDate = intermediateResult.size() < rss.getChannel().getItems().size();
            }
            url = getNextUrl(rss);
        } while (!(foundOlderThanMaxPubDate || url == null));

        return rv;
    }

    List<BikingPictureEntity> download(final List<BikingPictureEntity> list) {
        final List<BikingPictureEntity> rv = new ArrayList<>();

        list.forEach((BikingPictureEntity incoming) -> {
            final BikingPictureEntity existingPicture = this.bikingPictureRepository.findByExternalId(incoming.getExternalId());
            if (existingPicture == null) {
                synchronized (this) {
                    final URLConnection connection = this.dailyFratzeProvider.getImageConnection(incoming.getExternalId());
                    if (connection != null) {
                        try (InputStream inputStream = connection.getInputStream()) {
                            Files.copy(inputStream, new File(bikingPicturesStorage, String.format("%d.jpg", incoming.getExternalId())).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            rv.add(this.bikingPictureRepository.save(incoming));
                        } catch (IOException ex) {
                            log.error("Could not download image data, skipping!", ex);
                        }
                    }
                }
            }
        });

        return rv;
    }

    private RSS getRSSFeed(final String url) {
        final URLConnection connection = this.dailyFratzeProvider.getRSSConnection(url);
        RSS rss = null;
        if (connection != null) {
            try (InputStream inputStream = connection.getInputStream()) {
                final Unmarshaller unmarschaller = rssContext.createUnmarshaller();
                rss = (RSS) unmarschaller.unmarshal(inputStream);
            } catch (IOException | JAXBException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return rss;
    }

    private String getNextUrl(final RSS rss) {
        return rss == null ? null : rss.getChannel().getNext();
    }
}
