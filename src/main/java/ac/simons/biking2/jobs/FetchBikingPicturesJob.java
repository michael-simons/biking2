/*
 * Copyright 2014 Michael J. Simons.
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
package ac.simons.biking2.jobs;

import ac.simons.biking2.persistence.entities.BikingPicture;
import ac.simons.biking2.persistence.repositories.BikingPictureRepository;
import ac.simons.biking2.rss.RSS;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static ac.simons.biking2.config.DataStorageConfig.BIKING_PICTURES_DIRECTORY;
import static java.time.ZonedDateTime.ofInstant;
import static java.util.stream.Collectors.toList;

/**
 * @author Michael J. Simons, 2014-02-17
 */
@Component
@Profile({"dev", "prod"})
public class FetchBikingPicturesJob {

    private final DailyFratzeProvider dailyFratzeProvider;
    private final BikingPictureRepository bikingPictureRepository;
    private final File bikingPicturesStorage;
    private final JAXBContext rssContext;

    @Autowired
    public FetchBikingPicturesJob(
	    final DailyFratzeProvider dailyFratzeProvider,
	    final BikingPictureRepository bikingPictureRepository,
	    final File datastoreBaseDirectory
    ) {
	this.dailyFratzeProvider = dailyFratzeProvider;
	this.bikingPictureRepository = bikingPictureRepository;

	this.bikingPicturesStorage = new File(datastoreBaseDirectory, BIKING_PICTURES_DIRECTORY);
	if (!(this.bikingPicturesStorage.isDirectory() || this.bikingPicturesStorage.mkdirs())) {
	    throw new RuntimeException("Could not create bikingPicturesStorage!");
	}

	try {
	    this.rssContext = JAXBContext.newInstance(RSS.class);
	} catch (JAXBException e) {
	    throw new RuntimeException(e);
	}
    }

    @PostConstruct
    @Scheduled(cron = "${biking2.fetch-biking-picture-cron:0 0 */8 * * *}")
    public void run() {
	download(createDownloadList());
    }

    List<BikingPicture> createDownloadList() {
	final List<BikingPicture> rv = new ArrayList<>();

	// Current (or 1st) url
	String url = null;
	// Current rss data
	RSS rss = null;

	final Calendar hlp = this.bikingPictureRepository.getMaxPubDate();
	final ZonedDateTime maxPubDate = ofInstant(hlp.toInstant(), hlp.getTimeZone().toZoneId());
	boolean foundOlderThanMaxPubDate = false;
	do {
	    rss = getRSSFeed(url);
	    if (rss == null) {
		Logger.getLogger(FetchBikingPicturesJob.class.getName()).log(Level.WARNING, "There was a problem getting the feed data");
	    } else {
		final List<BikingPicture> intermediateResult
			= rss.getChannel()
			.getItems()
			.stream()
			.filter(item -> item.getPubDate().isAfter(maxPubDate))
			.map(item -> new BikingPicture(item.getGuid().getValue(), item.getPubDate(), item.getLink()))
			.collect(toList());
		rv.addAll(intermediateResult);
		foundOlderThanMaxPubDate = intermediateResult.size() < rss.getChannel().getItems().size();
	    }
	} while (!foundOlderThanMaxPubDate && (url = getNextUrl(rss)) != null);

	return rv;
    }

    List<BikingPicture> download(final List<BikingPicture> list) {
	final List<BikingPicture> rv = new ArrayList<>();

	list.forEach((BikingPicture incoming) -> {
	    final BikingPicture existingPicture = this.bikingPictureRepository.findByExternalId(incoming.getExternalId());
	    if (existingPicture == null) {
		synchronized (this) {
		    final URLConnection connection = this.dailyFratzeProvider.getImageConnection(incoming.getExternalId());
		    if (connection != null) {
			try (final InputStream inputStream = connection.getInputStream()) {
			    Files.copy(inputStream, new File(bikingPicturesStorage, String.format("%d.jpg", incoming.getExternalId())).toPath());
			    rv.add(this.bikingPictureRepository.save(incoming));			
			} catch (IOException ex) {
			    Logger.getLogger(FetchBikingPicturesJob.class.getName()).log(Level.SEVERE, "Could not download image data, skipping!", ex);
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
	    try (final InputStream inputStream = connection.getInputStream()) {
		final Unmarshaller unmarschaller = rssContext.createUnmarshaller();
		rss = (RSS) unmarschaller.unmarshal(inputStream);
	    } catch (IOException | JAXBException ex) {
		Logger.getLogger(FetchBikingPicturesJob.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	return rss;
    }

    private String getNextUrl(final RSS rss) {
	return rss == null ? null : rss.getChannel().getNext();
    }
}
