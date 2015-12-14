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

import ac.simons.biking2.bikingPictures.BikingPicturesControllerTest.RegexMatcher;
import ac.simons.biking2.config.DatastoreConfig;
import ac.simons.biking2.persistence.entities.Track;
import ac.simons.biking2.persistence.repositories.TrackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons, 2014-02-15
 */
public class TracksControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Track> defaultTestData;
    private final File tmpDir;        
    private final File tracksDir;
    private final File gpsBabel;
    
    @Rule
    public final ExpectedException expectedException = none();
    
    public TracksControllerTest() {
	final int[] ids = new int[]{1, 2, 3};
	final LocalDate now = LocalDate.now();
	final Random random = new Random(System.currentTimeMillis());

	this.defaultTestData = Stream.of(1, 2, 3).map(i -> {
	    final Track t = mock(Track.class);
	    stub(t.getId()).toReturn(i);
	    stub(t.getCoveredOn()).toReturn(GregorianCalendar.from(now.minusDays(random.nextInt(365)).atStartOfDay(ZoneId.systemDefault())));
	    return t;
	}).collect(toList());
	
	this.tmpDir = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()));
	this.tmpDir.deleteOnExit();
	this.tracksDir = new File(this.tmpDir, DatastoreConfig.TRACK_DIRECTORY);
	this.tracksDir.mkdirs();
	
	// Try to find gpsbabel
	File tmp = null;
	for(String possibleExe : new String[]{"/usr/bin/gpsbabel", "/opt/local/bin/gpsbabel"}) {
	    tmp = new File(possibleExe);
	    if(tmp.canExecute()) {
		break;
	    }
	}    
	if(tmp == null) {
	    throw new IllegalStateException("No gpsbabel found, cannot execute test!");
	}
	this.gpsBabel = tmp;
    }
     
    @Test
    public void testGetTracks() {
	final TrackRepository trackRepository = mock(TrackRepository.class);
	stub(trackRepository.findAll(new Sort(Sort.Direction.ASC, "coveredOn"))).toReturn(defaultTestData);
	final TracksController tracksController = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath());

	final List<Track> tracks = tracksController.getTracks();

	assertThat(tracks, is(equalTo(defaultTestData)));
    }
    
    @Test
    public void testDownloadTrack() throws IOException {
	final int validId = 23;
	
	final Track t = mock(Track.class);
	stub(t.getId()).toReturn(validId);	
	final TrackRepository trackRepository = mock(TrackRepository.class);
	stub(trackRepository.findOne(validId)).toReturn(t);
	
	final String content = "<foo>bar</foo>";
	final List<String> contentGpx = Arrays.asList(content, "gpx");
	final List<String> contentTcx = Arrays.asList(content, "tcx");
	
	final File trackGpx = new File(tracksDir, String.format("%d.gpx", validId));
	Files.write(trackGpx.toPath(), contentGpx);	
	trackGpx.deleteOnExit();
	stub(t.getTrackFile(tmpDir, "gpx")).toReturn(trackGpx);
	
	final File trackTcx = new File(tracksDir, String.format("%d.tcx", validId));
	Files.write(trackTcx.toPath(), contentTcx);	
	trackTcx.deleteOnExit();	
	stub(t.getTrackFile(tmpDir, "tcx")).toReturn(trackTcx);
		
	final TracksController tracksController = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath());
	
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	
	// Invalid formats...
	response = new MockHttpServletResponse();
	final String validPrettyId = Integer.toString(validId, 36);
	tracksController.downloadTrack(validPrettyId, "html", new MockHttpServletRequest(), response);
	assertThat(response.getStatus(), is(equalTo(HttpServletResponse.SC_NOT_ACCEPTABLE)));
	response = new MockHttpServletResponse();
	tracksController.downloadTrack(Integer.toString(validId, 36), "", new MockHttpServletRequest(), response);
	assertThat(response.getStatus(), is(equalTo(HttpServletResponse.SC_NOT_ACCEPTABLE)));
	response = new MockHttpServletResponse();
	tracksController.downloadTrack(Integer.toString(validId, 36), null, new MockHttpServletRequest(), response);
	assertThat(response.getStatus(), is(equalTo(HttpServletResponse.SC_NOT_ACCEPTABLE)));
	
	// invalid ids
	response = new MockHttpServletResponse();
	tracksController.downloadTrack("öäü", "gpx", new MockHttpServletRequest(), response);
	assertThat(response.getStatus(), is(equalTo(HttpServletResponse.SC_NOT_ACCEPTABLE)));	
	response = new MockHttpServletResponse();
	tracksController.downloadTrack("", "gpx", new MockHttpServletRequest(), response);
	assertThat(response.getStatus(), is(equalTo(HttpServletResponse.SC_NOT_ACCEPTABLE)));
	response = new MockHttpServletResponse();
	tracksController.downloadTrack(null, "gpx", new MockHttpServletRequest(), response);
	assertThat(response.getStatus(), is(equalTo(HttpServletResponse.SC_NOT_ACCEPTABLE)));
	response = new MockHttpServletResponse();
	tracksController.downloadTrack("1", "gpx", new MockHttpServletRequest(), response);
	assertThat(response.getStatus(), is(equalTo(HttpServletResponse.SC_NOT_FOUND)));
	
	request = new MockHttpServletRequest();
	request.setAttribute("org.apache.tomcat.sendfile.support", true);			
	response = new MockHttpServletResponse();
	tracksController.downloadTrack(Integer.toString(validId, 36), "gpx", request, response);	
	assertThat(response.getContentType(), is(equalTo("application/gpx+xml")));
	assertThat(response.getHeader("Content-Disposition"), is(equalTo(String.format("attachment; filename=\"%s.gpx\"", validPrettyId))));
	assertThat(request.getAttribute("org.apache.tomcat.sendfile.filename"), is(equalTo(trackGpx.getAbsolutePath())));
	assertThat(request.getAttribute("org.apache.tomcat.sendfile.start"), is(equalTo(0l)));
	assertThat(request.getAttribute("org.apache.tomcat.sendfile.end"), is(equalTo(trackGpx.length())));	
	
	request = new MockHttpServletRequest();
	request.setAttribute("org.apache.tomcat.sendfile.support", false);			
	response = new MockHttpServletResponse();
	tracksController.downloadTrack(Integer.toString(validId, 36), "gpx", request, response);	
	assertThat(response.getContentType(), is(equalTo("application/gpx+xml")));
	assertThat(response.getHeader("Content-Disposition"), is(equalTo(String.format("attachment; filename=\"%s.gpx\"", validPrettyId))));
	assertThat(response.getContentAsString(), is(equalTo(contentGpx.stream().collect(Collectors.joining("\n", "", "\n")))));
	
	request = new MockHttpServletRequest();	
	response = new MockHttpServletResponse();
	tracksController.downloadTrack(Integer.toString(validId, 36), "tcx", request, response);	
	assertThat(response.getContentType(), is(equalTo("application/xml")));
	assertThat(response.getHeader("Content-Disposition"), is(equalTo(String.format("attachment; filename=\"%s.tcx\"", validPrettyId))));	
	assertThat(response.getContentAsString(), is(equalTo(contentTcx.stream().collect(Collectors.joining("\n", "", "\n")))));
    }
    
    @Test
    public void testGetTrack() {
	final int validId = 23;
	
	final Track t = mock(Track.class);
	stub(t.getId()).toReturn(validId);	
	final TrackRepository trackRepository = mock(TrackRepository.class);
	stub(trackRepository.findOne(validId)).toReturn(t);
	
	final TracksController tracksController = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath());
	
	ResponseEntity<Track> response;
	response = tracksController.getTrack(Integer.toString(validId, 36));
	assertThat(response.getBody(), is(notNullValue()));
	assertThat(response.getBody().getId(), is(equalTo(validId)));
	
	response = tracksController.getTrack(null);
	assertThat(response.getBody(), is(CoreMatchers.nullValue()));
	assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_ACCEPTABLE)));
	
	response = tracksController.getTrack("");
	assertThat(response.getBody(), is(CoreMatchers.nullValue()));
	assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_ACCEPTABLE)));
	
	response = tracksController.getTrack("öäü");
	assertThat(response.getBody(), is(CoreMatchers.nullValue()));
	assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_ACCEPTABLE)));
	
	response = tracksController.getTrack("1");
	assertThat(response.getBody(), is(CoreMatchers.nullValue()));
	assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
    }
    
    @Test
    public void testCreateTrack() throws Exception {
	final Track track = new Track() {	    
	    private static final long serialVersionUID = -3391535625175956488L;

	    @Override
	    public Integer getId() {
		return 4711;
	    }	    

	    @Override
	    public String getPrettyId() {
		return Integer.toString(this.getId(), 36);
	    }
	};
	
	final TrackRepository trackRepository = mock(TrackRepository.class);
	stub(trackRepository.save(Matchers.any(Track.class))).toReturn(track);
	
	final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath());
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
	// Test valid track
	MockMultipartFile trackData = new MockMultipartFile("trackData", this.getClass().getResourceAsStream("/test.tcx"));
	mockMvc
		.perform(
			fileUpload("http://biking.michael-simons.eu/api/tracks")
			    .file(trackData)
			    .param("name", "name")
			    .param("coveredOn", "2014-01-01T21:21:21.000Z")
			    .param("description", "description")
			    .param("type", "biking")
		)
		.andDo(MockMvcResultHandlers.print())
		.andExpect(status().isOk())
		.andExpect(content().string(objectMapper.writeValueAsString(track)));
	Assert.assertTrue(new File(tmpDir, String.format("%s/%d.%s", DatastoreConfig.TRACK_DIRECTORY, track.getId(), "tcx")).isFile());
	Assert.assertTrue(new File(tmpDir, String.format("%s/%d.%s", DatastoreConfig.TRACK_DIRECTORY, track.getId(), "gpx")).isFile());
	
	trackData = new MockMultipartFile("trackData", this.getClass().getResourceAsStream("/biking_pictures.rss"));
	mockMvc
		.perform(
			fileUpload("http://biking.michael-simons.eu/api/tracks")
			    .file(trackData)
			    .param("name", "name")
			    .param("coveredOn", "2014-01-01T21:21:21.000Z")
			    .param("description", "description")
			    .param("type", "biking")
		)
		.andDo(MockMvcResultHandlers.print())
		.andExpect(status().isBadRequest());
	Assert.assertFalse("There must not be any files leftover", new File(tmpDir, String.format("%s/%d.%s", DatastoreConfig.TRACK_DIRECTORY, track.getId(), "tcx")).isFile());
	Assert.assertFalse("There must not be any files leftover", new File(tmpDir, String.format("%s/%d.%s", DatastoreConfig.TRACK_DIRECTORY, track.getId(), "gpx")).isFile());
	Mockito.verify(trackRepository, Mockito.times(1)).delete(track);
    }
    
    @Test
    public void shouldNotCreateInvalidTracks() throws Exception {
	final TrackRepository trackRepository = mock(TrackRepository.class);	
	final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath());
	
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	
	// Empty data
	final MockMultipartFile multipartFile = new MockMultipartFile("trackData", new byte[0]);		
	mockMvc
		.perform(
			fileUpload("http://biking.michael-simons.eu/api/tracks")
			    .file(multipartFile)
			    .param("name", "poef")
			    .param("coveredOn", "2014-03-24T23:00:00.000Z")
			    .param("description", "description")
		)		
		.andExpect(status().isBadRequest());
	
	// No data
	mockMvc
	    .perform(
		    fileUpload("http://biking.michael-simons.eu/api/tracks")
			.param("name", "poef")
			.param("coveredOn", "2014-03-24T23:00:00.000Z")
			.param("description", "description")
	    )	    
	    .andExpect(status().isBadRequest());
	
	Mockito.verifyZeroInteractions(trackRepository);
    }
    
    @Test
    public void shouldHandleDataIntegrityViolationsGracefully() throws Exception {
	final TrackRepository trackRepository = mock(TrackRepository.class);
	stub(trackRepository.save(Matchers.any(Track.class))).toThrow(new DataIntegrityViolationException("fud"));
	
	final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath());
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	
	MockMultipartFile trackData = new MockMultipartFile("trackData", this.getClass().getResourceAsStream("/test.tcx"));
	mockMvc
		.perform(
			fileUpload("http://biking.michael-simons.eu/api/tracks")
			    .file(trackData)
			    .param("name", "name")
			    .param("coveredOn", "2014-01-01T21:21:21.000Z")
			    .param("description", "description")
			    .param("type", "biking")
		)		
		.andExpect(status().isConflict());	
	
	Mockito.verify(trackRepository).save(Mockito.any(Track.class));
	Mockito.verifyNoMoreInteractions(trackRepository);
    }
    
    @Test
    public void shouldHandleIOExceptionsGracefully1() throws Exception {
	Track track = Mockito.mock(Track.class);
	Mockito.stub(track.getId()).toReturn(4223);
	Mockito.stub(track.getPrettyId()).toReturn(Integer.toString(4223, 36));
	// return a directory so that the controller cannot write to it
	Mockito.stub(track.getTrackFile(this.tmpDir, "tcx")).toReturn(this.tmpDir);
	
	final TrackRepository trackRepository = mock(TrackRepository.class);	
	final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath());
	
	this.expectedException.expect(RuntimeException.class);	
	this.expectedException.expectMessage(new RegexMatcher(".*\\(Is a directory\\)$"));
	controller.storeFile(track, new ByteArrayInputStream(new byte[0]));
	
	Mockito.verify(track);
	Mockito.verifyNoMoreInteractions(track);
    }
    
    @Test
    public void shouldHandleIOExceptionsGracefully2() throws Exception {
	Track track = Mockito.mock(Track.class);	
	Mockito.stub(track.getTrackFile(this.tmpDir, "tcx")).toReturn(File.createTempFile("4223", ".tcx"));
	Mockito.stub(track.getTrackFile(this.tmpDir, "gpx")).toReturn(File.createTempFile("4223", ".gpx"));
	
	final TrackRepository trackRepository = mock(TrackRepository.class);	
	final TracksController controller = new TracksController(trackRepository, this.tmpDir, new File("/iam/not/gpsBabel").getAbsolutePath());
	
	this.expectedException.expect(RuntimeException.class);	
	this.expectedException.expectMessage("java.io.IOException: Cannot run program \"/iam/not/gpsBabel\": error=2, No such file or directory");
	controller.storeFile(track, new ByteArrayInputStream(new byte[0]));
	
	Mockito.verify(track);
	Mockito.verifyNoMoreInteractions(track);
    }
    
    @Test
    public void shouldHandleInvalidTcxFiles() throws Exception {
	Track track = Mockito.mock(Track.class);
	Mockito.stub(track.getTrackFile(this.tmpDir, "tcx")).toReturn(File.createTempFile("4223", ".tcx"));
	Mockito.stub(track.getTrackFile(this.tmpDir, "gpx")).toReturn(File.createTempFile("4223", ".gpx"));
	
	final TrackRepository trackRepository = mock(TrackRepository.class);	
	final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath());
	
	this.expectedException.expect(RuntimeException.class);	
	this.expectedException.expectMessage("GPSBabel could not convert the input file!");
	controller.storeFile(track, this.getClass().getResourceAsStream("/test-invalid.tcx"));
	
	Mockito.verify(track);
	Mockito.verifyNoMoreInteractions(track);
    }
}
