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

import ac.simons.biking2.api.model.Coordinate;
import ac.simons.biking2.persistence.entities.Track;
import ac.simons.biking2.persistence.repositories.TrackRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Michael J. Simons, 2014-02-15
 */
public class TracksControllerTest {

    private final List<Track> defaultTestData;
    
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
    }
     
    @Test
    public void testGetTracks() {
	final TrackRepository trackRepository = mock(TrackRepository.class);
	stub(trackRepository.findAll(new Sort(Sort.Direction.ASC, "coveredOn"))).toReturn(defaultTestData);
	final TracksController tracksController = new TracksController(trackRepository, new File(System.getProperty("java.io.tmpdir")));

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
	
	final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
	final File tracksDir = new File(tmpDir, "data/tracks");
	tracksDir.mkdirs();	
	
	final String content = "<foo>bar</foo>";
	final List<String> contentGpx = Arrays.asList(content, "gpx");
	final List<String> contentTcx = Arrays.asList(content, "tcx");
	
	final File trackGpx = new File(tracksDir, String.format("%d.gpx", validId));
	Files.write(trackGpx.toPath(), contentGpx);
	trackGpx.createNewFile();
	trackGpx.deleteOnExit();
	
	final File trackTcx = new File(tracksDir, String.format("%d.tcx", validId));
	Files.write(trackTcx.toPath(), contentTcx);
	trackTcx.createNewFile();
	trackTcx.deleteOnExit();
	
	final TracksController tracksController = new TracksController(trackRepository, tmpDir);
	
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
    
    public void testGetTrack() {
	final int validId = 23;
	
	final Track t = mock(Track.class);
	stub(t.getId()).toReturn(validId);	
	final TrackRepository trackRepository = mock(TrackRepository.class);
	stub(trackRepository.findOne(validId)).toReturn(t);
	
	final TracksController tracksController = new TracksController(trackRepository, new File(System.getProperty("java.io.tmpdir")));
	
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
}
