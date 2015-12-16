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
package ac.simons.biking2.galleryPictures;

import ac.simons.biking2.config.DatastoreConfig;
import ac.simons.biking2.persistence.entities.GalleryPicture;
import ac.simons.biking2.persistence.repositories.GalleryPictureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static java.util.TimeZone.getTimeZone;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
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
	this.galleryPictures = new File(this.tmpDir, DatastoreConfig.GALLERY_PICTURES_DIRECTORY);
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
		.andExpect(status().isOk())
		.andExpect(content().string(objectMapper.writeValueAsString(galleryPicture)));
    }
    
    @Test
    public void shouldHandleIOExceptionsGracefully() throws Exception {
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
	// use non existing dir
	final GalleryController controller = new GalleryController(repository, new File(this.tmpDir, "haha, got you"));

	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	final MockMultipartFile multipartFile = new MockMultipartFile("imageData", this.getClass().getResourceAsStream("/IMG_0041.JPG"));

	mockMvc
		.perform(
			fileUpload("http://biking.michael-simons.eu/api/galleryPictures")
			.file(multipartFile)
			.param("takenOn", "2014-03-24T23:00:00.000Z")
			.param("description", "description")
		)		
		.andExpect(status().isInternalServerError());
    }
    
    @Test
    public void shouldHandleDataIntegrityViolationsGracefully() throws Exception {
	final GalleryPictureRepository repository = mock(GalleryPictureRepository.class);	
	stub(repository.save(Mockito.any(GalleryPicture.class))).toThrow(new DataIntegrityViolationException("fud"));

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
		.andExpect(status().isBadRequest());
    }
    
    @Test
    public void shouldNotCreateInvalidGalleryPicture() throws Exception {
	final GalleryPictureRepository repository = mock(GalleryPictureRepository.class);	
	final GalleryController controller = new GalleryController(repository, this.tmpDir);

	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	
	// Empty data
	final MockMultipartFile multipartFile = new MockMultipartFile("imageData", new byte[0]);		
	mockMvc
		.perform(
			fileUpload("http://biking.michael-simons.eu/api/galleryPictures")
			    .file(multipartFile)
			    .param("takenOn", "2014-03-24T23:00:00.000Z")
			    .param("description", "description")
		)
		.andDo(MockMvcResultHandlers.print())
		.andExpect(status().isBadRequest());
	
	// No data
	mockMvc
	    .perform(
		    fileUpload("http://biking.michael-simons.eu/api/galleryPictures")		
			.param("takenOn", "2014-03-24T23:00:00.000Z")
			.param("description", "description")
	    )
	    .andDo(MockMvcResultHandlers.print())
	    .andExpect(status().isBadRequest());
    }
    
    @Test
    public void shouldNotFindNonExistingPicture() throws Exception {
	final GalleryPictureRepository repository = mock(GalleryPictureRepository.class);	
	final GalleryController controller = new GalleryController(repository, this.tmpDir);

	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	mockMvc
	    .perform(
		    MockMvcRequestBuilders.get("http://biking.michael-simons.eu/api/galleryPictures/23.jpg")		

	    )
	    .andDo(MockMvcResultHandlers.print())
	    .andExpect(status().isNotFound());	
	
	Mockito.verify(repository).findOne(23);
	Mockito.verifyNoMoreInteractions(repository);
    }
    
    @Test
    public void shouldFindPicture() throws Exception {
	// Read an image resource into memory...
	final File imageFile = new File(this.galleryPictures, System.currentTimeMillis() + ".jpg");		
	final byte[] imageData;
	try (
	    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    final InputStream in = this.getClass().getResourceAsStream("/45325.jpg");
	) {
	    final byte[] buffer = new byte[2048];
	    int len;
	    while ((len = in.read(buffer, 0, buffer.length)) > 0) {
		bytes.write(buffer, 0, len);
	    }
	    in.close();		
	    bytes.flush();
	    imageData = bytes.toByteArray();
	}
	// Copy data to a file
	Files.copy(new ByteArrayInputStream(imageData), imageFile.toPath());
	
	final GalleryPictureRepository repository = mock(GalleryPictureRepository.class);		
	Mockito.stub(repository.findOne(42)).toReturn(new GalleryPicture(Calendar.getInstance(), imageFile.getName()));
	
	final GalleryController controller = new GalleryController(repository, this.tmpDir);

	// Test copying of resources
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	// Explicit false property
	final MvcResult result = 
	    mockMvc
		.perform(
			MockMvcRequestBuilders
			    .get("http://biking.michael-simons.eu/api/galleryPictures/42.jpg")							    
			    .requestAttr("org.apache.tomcat.sendfile.support", false)
		)	    
		.andExpect(status().isOk())
		.andExpect(request().attribute("org.apache.tomcat.sendfile.filename", CoreMatchers.nullValue()))
		.andReturn();
	Assert.assertTrue(Arrays.equals(imageData, result.getResponse().getContentAsByteArray()));	
	// Null request (Don't know how to make the request null using MockMvcBuilders)
	MockHttpServletResponse response = new MockHttpServletResponse();
	controller.getGalleryPicture(42, null, response);
	Assert.assertTrue(Arrays.equals(imageData, response.getContentAsByteArray()));
	
	// Test streaming of resources
	mockMvc
	    .perform(
		    MockMvcRequestBuilders
			.get("http://biking.michael-simons.eu/api/galleryPictures/42.jpg")							    
			.requestAttr("org.apache.tomcat.sendfile.support", true)
	    )	    
	    .andExpect(status().isOk())
	    .andExpect(request().attribute("org.apache.tomcat.sendfile.filename", imageFile.getAbsolutePath()))
	    .andExpect(request().attribute("org.apache.tomcat.sendfile.start", 0l))
	    .andExpect(request().attribute("org.apache.tomcat.sendfile.end", imageFile.length()))
	    .andExpect(header().longValue("Content-Length", imageFile.length()))
	    .andReturn();	
	
	Mockito.verify(repository, times(3)).findOne(42);
	Mockito.verifyNoMoreInteractions(repository);
    }
    
    @Test
    public void shouldGetGalleryPictures() {
	final GalleryPictureRepository repository = mock(GalleryPictureRepository.class);	
	Mockito.stub(repository.findAll(Mockito.any(Sort.class))).toReturn(new ArrayList<>());
	final GalleryController controller = new GalleryController(repository, this.tmpDir);

	final List<GalleryPicture> galleryPictures = controller.getGalleryPictures();
	Assert.assertNotNull(galleryPictures);
	Assert.assertEquals(0, galleryPictures.size());
	
	Mockito.verify(repository).findAll(Mockito.any(Sort.class));
	Mockito.verifyNoMoreInteractions(repository);
    }
}
