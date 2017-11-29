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
package ac.simons.biking2.tracks;

import ac.simons.biking2.support.RegexMatcher;
import ac.simons.biking2.config.DatastoreConfig;
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
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons, 2014-02-15
 */
public class TracksControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<TrackEntity> defaultTestData;
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
            final TrackEntity t = mock(TrackEntity.class);
            when(t.getId()).thenReturn(i);
            when(t.getCoveredOn()).thenReturn(GregorianCalendar.from(now.minusDays(random.nextInt(365)).atStartOfDay(ZoneId.systemDefault())));
            return t;
        }).collect(toList());

        this.tmpDir = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()));
        this.tmpDir.deleteOnExit();
        this.tracksDir = new File(this.tmpDir, DatastoreConfig.TRACK_DIRECTORY);
        this.tracksDir.mkdirs();

        // Try to find gpsbabel
        File tmp = null;
        for(String possibleExe : new String[]{"/usr/bin/gpsbabel", "/usr/local/bin/gpsbabel", "/opt/local/bin/gpsbabel"}) {
            tmp = new File(possibleExe);
            if(tmp.canExecute()) {
                break;
            }
			tmp = null;
        }
        if(tmp == null) {
            throw new IllegalStateException("No gpsbabel found, cannot execute test!");
        }
        this.gpsBabel = tmp;
    }

    @Test
    public void testGetTracks() {
        final TrackRepository trackRepository = mock(TrackRepository.class);
        when(trackRepository.findAll(new Sort(Sort.Direction.ASC, "coveredOn"))).thenReturn(defaultTestData);
        final TracksController tracksController = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), null);

        final List<TrackEntity> tracks = tracksController.getTracks();

        assertThat(tracks, is(equalTo(defaultTestData)));
    }

    @Test
    public void testDownloadTrack() throws IOException {
        final int validId = 23;

        final TrackEntity t = mock(TrackEntity.class);
        when(t.getId()).thenReturn(validId);
        final TrackRepository trackRepository = mock(TrackRepository.class);
        when(trackRepository.findById(validId)).thenReturn(Optional.of(t));

        final String content = "<foo>bar</foo>";
        final List<String> contentGpx = Arrays.asList(content, "gpx");
        final List<String> contentTcx = Arrays.asList(content, "tcx");

        final File trackGpx = new File(tracksDir, String.format("%d.gpx", validId));
        Files.write(trackGpx.toPath(), contentGpx);
        trackGpx.deleteOnExit();
        when(t.getTrackFile(tmpDir, "gpx")).thenReturn(trackGpx);

        final File trackTcx = new File(tracksDir, String.format("%d.tcx", validId));
        Files.write(trackTcx.toPath(), contentTcx);
        trackTcx.deleteOnExit();
        when(t.getTrackFile(tmpDir, "tcx")).thenReturn(trackTcx);

        final TracksController tracksController = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), null);

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

        final TrackEntity t = mock(TrackEntity.class);
        when(t.getId()).thenReturn(validId);
        final TrackRepository trackRepository = mock(TrackRepository.class);
        when(trackRepository.findById(validId)).thenReturn(Optional.of(t));

        final TracksController tracksController = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), null);

        ResponseEntity<TrackEntity> response;
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
    public void testDeleteTrack() throws IOException {
        final int validId = 23;

        final TrackRepository trackRepository = mock(TrackRepository.class);

        TrackEntity t;
        t = mock(TrackEntity.class);
        when(t.getId()).thenReturn(validId);
        when(t.getTrackFile(any(File.class), same("gpx"))).thenReturn(new File("fump"));
        when(t.getTrackFile(any(File.class), same("tcx"))).thenReturn(new File("zack"));

        when(trackRepository.findById(validId)).thenReturn(Optional.of(t));

        t = mock(TrackEntity.class);
        when(t.getId()).thenReturn(validId + 1);
        final File gpx = File.createTempFile("pppp-", ".gpx");
        when(t.getTrackFile(any(File.class), same("gpx"))).thenReturn(gpx);
        final File tcx = File.createTempFile("pppp-", ".tcx");
        when(t.getTrackFile(any(File.class), same("tcx"))).thenReturn(tcx);
        when(trackRepository.findById(validId + 1)).thenReturn(Optional.of(t));

        final TracksController tracksController = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), null);
        ResponseEntity<Void> response;

        response = tracksController.deleteTrack(null);
        assertThat(response.getBody(), is(CoreMatchers.nullValue()));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_ACCEPTABLE)));

        response = tracksController.deleteTrack("");
        assertThat(response.getBody(), is(CoreMatchers.nullValue()));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_ACCEPTABLE)));

        response = tracksController.deleteTrack("öäü");
        assertThat(response.getBody(), is(CoreMatchers.nullValue()));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_ACCEPTABLE)));

        response = tracksController.deleteTrack("1");
        assertThat(response.getBody(), is(CoreMatchers.nullValue()));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));

        response = tracksController.deleteTrack(Integer.toString(validId, 36));
        assertThat(response.getBody(), is(CoreMatchers.nullValue()));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.INTERNAL_SERVER_ERROR)));

        assertTrue(gpx.exists() && gpx.exists());
        response = tracksController.deleteTrack(Integer.toString(validId +1, 36));
        assertThat(response.getBody(), is(CoreMatchers.nullValue()));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
        assertFalse(gpx.exists() && gpx.exists());
    }

    @Test
    public void testCreateTrack() throws Exception {
        final TrackEntity track = new TrackEntity() {
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
        when(trackRepository.save(any(TrackEntity.class))).thenReturn(track);

        final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), null);
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
        final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), null);

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

        verifyZeroInteractions(trackRepository);
    }

    @Test
    public void shouldHandleDataIntegrityViolationsGracefully() throws Exception {
        final TrackRepository trackRepository = mock(TrackRepository.class);
        when(trackRepository.save(any(TrackEntity.class))).thenThrow(new DataIntegrityViolationException("fud"));

        final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), null);
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

        Mockito.verify(trackRepository).save(Mockito.any(TrackEntity.class));
        Mockito.verifyNoMoreInteractions(trackRepository);
    }

    @Test
    public void shouldHandleIOExceptionsGracefully1() throws Exception {
        TrackEntity track = Mockito.mock(TrackEntity.class);
        when(track.getId()).thenReturn(4223);
        when(track.getPrettyId()).thenReturn(Integer.toString(4223, 36));
        // return a directory so that the controller cannot write to it
        when(track.getTrackFile(this.tmpDir, "tcx")).thenReturn(this.tmpDir);

        final TrackRepository trackRepository = mock(TrackRepository.class);
        final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), null);

        this.expectedException.expect(FileNotFoundException.class);	
        controller.storeFile(track, new ByteArrayInputStream(new byte[0]));

        verify(track).getId();
        verify(track).getPrettyId();
        verify(track).getTrackFile(this.tmpDir, "tcx");
        verifyNoMoreInteractions(track);
    }

    @Test
    public void shouldHandleIOExceptionsGracefully2() throws Exception {
        TrackEntity track = Mockito.mock(TrackEntity.class);
        when(track.getTrackFile(this.tmpDir, "tcx")).thenReturn(File.createTempFile("4223", ".tcx"));
        when(track.getTrackFile(this.tmpDir, "gpx")).thenReturn(File.createTempFile("4223", ".gpx"));

        final TrackRepository trackRepository = mock(TrackRepository.class);
        final TracksController controller = new TracksController(trackRepository, this.tmpDir, new File("/iam/not/gpsBabel").getAbsolutePath(), null);

        this.expectedException.expect(IOException.class);
        this.expectedException.expectMessage(new RegexMatcher("Cannot run program \"/iam/not/gpsBabel\": error=2,.+"));
        controller.storeFile(track, new ByteArrayInputStream(new byte[0]));

        Mockito.verify(track).getTrackFile(this.tmpDir, "tcx");
        Mockito.verify(track).getTrackFile(this.tmpDir, "gpx");
        Mockito.verifyNoMoreInteractions(track);
    }

    @Test
    public void shouldHandleInvalidTcxFiles() throws Exception {
        TrackEntity track = Mockito.mock(TrackEntity.class);
        when(track.getTrackFile(this.tmpDir, "tcx")).thenReturn(File.createTempFile("4223", ".tcx"));
        when(track.getTrackFile(this.tmpDir, "gpx")).thenReturn(File.createTempFile("4223", ".gpx"));

        final TrackRepository trackRepository = mock(TrackRepository.class);
        final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), null);

        this.expectedException.expect(RuntimeException.class);
        this.expectedException.expectMessage("GPSBabel could not convert the input file!");
        controller.storeFile(track, this.getClass().getResourceAsStream("/test-invalid.tcx"));

        verify(track).getTrackFile(this.tmpDir, "tcx");
        verify(track).getTrackFile(this.tmpDir, "gpx");
        verifyNoMoreInteractions(track);
    }

    @Test
    public void testGetHome() {
        final Coordinate home = new Coordinate("13.408056", "52.518611");
        final TrackRepository trackRepository = mock(TrackRepository.class);
        final TracksController controller = new TracksController(trackRepository, this.tmpDir, this.gpsBabel.getAbsolutePath(), home);

        Assert.assertEquals(home, controller.getHome());
    }
}
