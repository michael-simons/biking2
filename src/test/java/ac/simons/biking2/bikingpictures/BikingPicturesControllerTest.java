/*
 * Copyright 2014-2017 michael-simons.eu.
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

import ac.simons.biking2.bikingpictures.rss.RSSDateTimeAdapter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static ac.simons.biking2.config.DatastoreConfig.BIKING_PICTURES_DIRECTORY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ac.simons.biking2.support.RegexMatcher.matches;
import java.util.Optional;

/**
 * @author Michael J. Simons, 2014-02-19
 */
public class BikingPicturesControllerTest {
    
    private final RSSDateTimeAdapter dateTimeAdapter = new RSSDateTimeAdapter();
    private final File tmpDir;
    private final File bikingPictures;
    private final byte[] testData;

    public BikingPicturesControllerTest() throws IOException {
        this.tmpDir = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()));
        this.tmpDir.deleteOnExit();
        this.bikingPictures = new File(this.tmpDir, BIKING_PICTURES_DIRECTORY);
        this.bikingPictures.mkdirs();
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (
                final InputStream in = this.getClass().getResourceAsStream("/45644.jpg");
                final FileOutputStream out = new FileOutputStream(new File(bikingPictures, "45644.jpg"))) {
            final byte[] buffer = new byte[2048];
            int len;
            while ((len = in.read(buffer, 0, buffer.length)) > 0) {
                bytes.write(buffer, 0, len);
            }
            bytes.flush();
            this.testData = bytes.toByteArray();
            out.getChannel().transferFrom(Channels.newChannel(new ByteArrayInputStream(this.testData)), 0, this.testData.length);
            out.flush();
        }
    }

    @Test
    public void testGetBikingPicture() throws Exception {
        final BikingPictureRepository repository = mock(BikingPictureRepository.class);
        when(repository.findById(1)).thenReturn(Optional.of(new BikingPictureEntity("http://dailyfratze.de/fratzen/m/45644.jpg", dateTimeAdapter.unmarshal("Sun, 12 Jan 2014 21:40:25 GMT"), "http://dailyfratze.de/michael/2014/1/12")));

        final BikingPicturesController controller = new BikingPicturesController(repository, tmpDir);
        final ZonedDateTime expiresIn = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(365);

        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/bikingPictures/23.jpg"))
                .andExpect(status().isNotFound());

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/bikingPictures/1.jpg").requestAttr("org.apache.tomcat.sendfile.support", false))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"1.jpg\""))
                .andExpect(header().string("Cache-Control", String.format("max-age=%d, %s", 365 * 24 * 60 * 60, "public")))
                .andExpect(header().string("Expires", matches(expiresIn.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:'\\d{2}' 'GMT'").withLocale(Locale.US)))))
                .andExpect(content().bytes(testData));

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/bikingPictures/1.jpg").requestAttr("org.apache.tomcat.sendfile.support", true))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"1.jpg\""))
                .andExpect(request().attribute("org.apache.tomcat.sendfile.filename", new File(bikingPictures, "45644.jpg").getAbsolutePath()))
                .andExpect(request().attribute("org.apache.tomcat.sendfile.start", 0l))
                .andExpect(request().attribute("org.apache.tomcat.sendfile.end", (long) this.testData.length));
    }

    @Test
    public void shouldGetGalleryPictures() {
        final BikingPictureRepository repository = mock(BikingPictureRepository.class);
        when(repository.findAll(Mockito.any(Sort.class))).thenReturn(new ArrayList<>());
        final BikingPicturesController controller = new BikingPicturesController(repository, this.tmpDir);

        final List<BikingPictureEntity> pictures = controller.getBikingPictures();
        Assert.assertNotNull(pictures);
        Assert.assertEquals(0, pictures.size());

        Mockito.verify(repository).findAll(Mockito.any(Sort.class));
        Mockito.verifyNoMoreInteractions(repository);
    }

}
