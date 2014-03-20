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
package ac.simons.biking2.api;

import ac.simons.biking2.config.PersistenceConfig;
import ac.simons.biking2.persistence.entities.GalleryPicture;
import ac.simons.biking2.persistence.repositories.GalleryPictureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.GregorianCalendar;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static java.util.TimeZone.getTimeZone;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons, 2014-03-07
 */
public class GalleryControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File tmpDir;
    private final File galleryPictures;

    public GalleryControllerTest() {
	this.tmpDir = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()));
	this.tmpDir.deleteOnExit();
	this.galleryPictures = new File(this.tmpDir, PersistenceConfig.GALLERY_PICTURES_DIRECTORY);
	this.galleryPictures.mkdirs();
    }

    @Test
    public void createGalleryPicture() throws Exception {
	final GalleryPictureRepository repository = mock(GalleryPictureRepository.class);
	final GregorianCalendar takenOn = new GregorianCalendar(getTimeZone("UTC"));
	takenOn.set(2014, 2, 24, 23, 0, 0);
	final GalleryPicture galleryPicture = new GalleryPicture(takenOn, "description") {
	    private static final long serialVersionUID = -3391535625175956488L;

	    @Override
	    public Integer getId() {
		return 23;
	    }
	};
	stub(repository.save(Mockito.any(GalleryPicture.class))).toReturn(galleryPicture);
	final GalleryController controller = new GalleryController(repository, this.tmpDir);

	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	final MockMultipartFile multipartFile = new MockMultipartFile("imageData", this.getClass().getResourceAsStream("/IMG_0041.JPG"));

	mockMvc
		.perform(
			fileUpload("http://biking.michael-simons.eu/api/galleryPictures")
			.file(multipartFile)
			.param("takenOn", "2014-03-24T23:00:00.000Z")
			.param("description", "description")
		)
		.andDo(MockMvcResultHandlers.print())
		.andExpect(status().isOk())
		.andExpect(content().string(objectMapper.writeValueAsString(galleryPicture)));
    }
}
