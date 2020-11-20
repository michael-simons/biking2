/*
 * Copyright 2014-2020 michael-simons.eu.
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

import ac.simons.biking2.config.DatastoreConfig;
import ac.simons.biking2.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons
 *
 * @since 2014-02-15
 */
@WebMvcTest(
        includeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = SecurityConfig.class),
        controllers = TracksController.class
)
class TracksControllerTest {

    private final List<TrackEntity> defaultTestData;

    @MockBean
    private TrackRepository trackRepository;

    @MockBean
    private TrackIdParser trackIdParser;

    @Autowired
    private File datastoreBaseDirectory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Coordinate home;

    @Autowired
    private MockMvc mockMvc;

    TracksControllerTest() {
        final LocalDate now = LocalDate.now();
        final Random random = new Random(System.currentTimeMillis());

        this.defaultTestData = Stream.of(1, 2, 3).map(i ->
            new TrackEntity(Integer.toString(i), now.minusDays(random.nextInt(365)))
        ).collect(toList());
    }

    @Test
    void testGetTracks() throws Exception {
        final Sort sort = Sort.by("coveredOn").ascending();
        when(trackRepository.findAll(sort)).thenReturn(defaultTestData);

        mockMvc
            .perform(get("http://biking.michael-simons.eu/api/tracks"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(this.defaultTestData)));

        verify(this.trackRepository).findAll(sort);
    }

    @Test
    void testDownloadTrack() throws Exception {
        final int validId = 23;
        final String validPrettyId = Integer.toString(validId, 36);
        when(trackIdParser.fromPrettyId(validPrettyId)).thenReturn(validId);
        when(trackIdParser.fromPrettyId("X")).thenReturn(null);

        final TrackEntity t = mock(TrackEntity.class);
        when(t.getId()).thenReturn(validId);
        when(trackRepository.findById(validId)).thenReturn(Optional.of(t));

        final String content = "<foo>bar</foo>";
        final List<String> contentGpx = Arrays.asList(content, "gpx");
        final List<String> contentTcx = Arrays.asList(content, "tcx");

        final File tracksDir = new File(this.datastoreBaseDirectory, DatastoreConfig.TRACK_DIRECTORY);
        final File trackGpx = new File(tracksDir, String.format("%d.gpx", validId));
        Files.write(trackGpx.toPath(), contentGpx);
        trackGpx.deleteOnExit();
        when(t.getTrackFile(datastoreBaseDirectory, "gpx")).thenReturn(trackGpx);

        final File trackTcx = new File(tracksDir, String.format("%d.tcx", validId));
        Files.write(trackTcx.toPath(), contentTcx);
        trackTcx.deleteOnExit();
        when(t.getTrackFile(datastoreBaseDirectory, "tcx")).thenReturn(trackTcx);

        // Invalid formats...
        mockMvc.perform(get("http://biking.michael-simons.eu/tracks/{id}.html", validPrettyId))
            .andExpect(status().isNotAcceptable());
        mockMvc.perform(get("http://biking.michael-simons.eu/tracks/{id}.{format}", validPrettyId, ""))
            .andExpect(status().isNotAcceptable());
        mockMvc.perform(get("http://biking.michael-simons.eu/tracks/{id}.{format}", validPrettyId, null))
            .andExpect(status().isNotAcceptable());

        // invalid ids
        mockMvc.perform(get("http://biking.michael-simons.eu/tracks/{id}.gpx", "X"))
            .andExpect(status().isNotAcceptable());
        mockMvc.perform(get("http://biking.michael-simons.eu/tracks/{id}.gpx", "1"))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("http://biking.michael-simons.eu/tracks/{id}.gpx", validPrettyId)
                .requestAttr("org.apache.tomcat.sendfile.support", true))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/gpx+xml"))
            .andExpect(header().stringValues("Content-Disposition", String.format("attachment; filename=\"%s.gpx\"", validPrettyId)))
            .andExpect(request().attribute("org.apache.tomcat.sendfile.filename", trackGpx.getAbsolutePath()))
            .andExpect(request().attribute("org.apache.tomcat.sendfile.start", 0l))
            .andExpect(request().attribute("org.apache.tomcat.sendfile.end", trackGpx.length()));

        mockMvc.perform(get("http://biking.michael-simons.eu/tracks/{id}.gpx", validPrettyId)
            .requestAttr("org.apache.tomcat.sendfile.support", false))
            .andExpect(status().isOk())
            .andExpect(header().stringValues("Content-Disposition", String.format("attachment; filename=\"%s.gpx\"", validPrettyId)))
            .andExpect(request().attribute("org.apache.tomcat.sendfile.filename", nullValue()))
            .andExpect(content().contentType("application/gpx+xml"))
            .andExpect(content().string(contentGpx.stream().collect(Collectors.joining("\n", "", "\n"))));

        mockMvc.perform(get("http://biking.michael-simons.eu/tracks/{id}.tcx", validPrettyId))
            .andExpect(status().isOk())
            .andExpect(header().stringValues("Content-Disposition", String.format("attachment; filename=\"%s.tcx\"", validPrettyId)))
            .andExpect(content().contentType("application/xml"))
            .andExpect(content().string(contentTcx.stream().collect(Collectors.joining("\n", "", "\n"))));
    }

    @Test
    void testGetTrack() throws Exception {
        final int validId = 23;
        final String validPrettyId = Integer.toString(validId, 36);
        when(trackIdParser.fromPrettyId(validPrettyId)).thenReturn(validId);
        when(trackIdParser.fromPrettyId("X")).thenReturn(null);

        final TrackEntity t = mock(TrackEntity.class);
        when(t.getId()).thenReturn(validId);
        when(t.getName()).thenReturn("testtrack");
        when(trackRepository.findById(validId)).thenReturn(Optional.of(t));

        mockMvc.perform(get("http://biking.michael-simons.eu/api/tracks/{id}", validPrettyId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("testtrack")));

        mockMvc.perform(get("http://biking.michael-simons.eu/api/tracks/{id}", "X"))
            .andExpect(status().isNotAcceptable());

        mockMvc.perform(get("http://biking.michael-simons.eu/api/tracks/{id}", "1"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteTrack() throws Exception {
        final int validId = 23;
        final int validExistingId = 23 + 1;
        final String validPrettyId = Integer.toString(validId, 36);
        final String validPrettyExistingId = Integer.toString(validExistingId, 36);
        when(trackIdParser.fromPrettyId(validPrettyId)).thenReturn(validId);
        when(trackIdParser.fromPrettyId(validPrettyExistingId)).thenReturn(validExistingId);
        when(trackIdParser.fromPrettyId("X")).thenReturn(null);
        
        TrackEntity t;
        t = mock(TrackEntity.class);
        when(t.getId()).thenReturn(validId);
        when(t.getTrackFile(any(File.class), same("gpx"))).thenReturn(new File("fump"));
        when(t.getTrackFile(any(File.class), same("tcx"))).thenReturn(new File("zack"));

        when(trackRepository.findById(validId)).thenReturn(Optional.of(t));

        t = mock(TrackEntity.class);
        when(t.getId()).thenReturn(validExistingId);
        final File gpx = File.createTempFile("pppp-", ".gpx");
        when(t.getTrackFile(any(File.class), same("gpx"))).thenReturn(gpx);
        final File tcx = File.createTempFile("pppp-", ".tcx");
        when(t.getTrackFile(any(File.class), same("tcx"))).thenReturn(tcx);
        when(trackRepository.findById(validExistingId)).thenReturn(Optional.of(t));
        
        // invalid ids
        mockMvc.perform(delete("http://biking.michael-simons.eu/api/tracks/{id}", "X"))
            .andExpect(status().isNotAcceptable());
        mockMvc.perform(delete("http://biking.michael-simons.eu/api/tracks/{id}", "1"))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("http://biking.michael-simons.eu/api/tracks/{id}.html", validPrettyId))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("http://biking.michael-simons.eu/api/tracks/{id}", validPrettyId))
            .andExpect(status().isInternalServerError());
        
        assertThat(gpx.exists()).isTrue();
        assertThat(tcx.exists()).isTrue();
        mockMvc.perform(delete("http://biking.michael-simons.eu/api/tracks/{id}", validPrettyExistingId))
            .andExpect(status().isNoContent());
        assertThat(gpx.exists()).isFalse();
        assertThat(tcx.exists()).isFalse();
    }

    @Test
    @WithMockUser
    void testCreateTrack() throws Exception {
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

        when(trackRepository.save(any(TrackEntity.class))).thenReturn(track);

        // Test valid track
        MockMultipartFile trackData = new MockMultipartFile("trackData", this.getClass().getResourceAsStream("/test.tcx"));
        mockMvc
                .perform(
                        multipart("http://biking.michael-simons.eu/api/tracks")
                            .file(trackData)
                            .param("name", "name")
                            .param("coveredOn", "2014-01-01T21:21:21.000Z")
                            .param("description", "description")
                            .param("type", "biking")
                )
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(track)));
        assertThat(new File(datastoreBaseDirectory, String.format("%s/%d.%s", DatastoreConfig.TRACK_DIRECTORY, track.getId(), "tcx")).isFile()).isTrue();
        assertThat(new File(datastoreBaseDirectory, String.format("%s/%d.%s", DatastoreConfig.TRACK_DIRECTORY, track.getId(), "gpx")).isFile()).isTrue();

        trackData = new MockMultipartFile("trackData", this.getClass().getResourceAsStream("/biking_pictures.rss"));
        mockMvc
                .perform(
                        multipart("http://biking.michael-simons.eu/api/tracks")
                            .file(trackData)
                            .param("name", "name")
                            .param("coveredOn", "2014-01-01T21:21:21.000Z")
                            .param("description", "description")
                            .param("type", "biking")
                )
                .andExpect(status().isBadRequest());
        assertThat(new File(datastoreBaseDirectory, String.format("%s/%d.%s", DatastoreConfig.TRACK_DIRECTORY, track.getId(), "tcx")).isFile())
            .withFailMessage("There must not be any files leftover")
            .isFalse();
        assertThat(new File(datastoreBaseDirectory, String.format("%s/%d.%s", DatastoreConfig.TRACK_DIRECTORY, track.getId(), "gpx")).isFile())
            .withFailMessage("There must not be any files leftover")
            .isFalse();
        verify(trackRepository, times(1)).delete(track);
    }

    @Test
    @WithMockUser
    void shouldNotCreateInvalidTracks() throws Exception {
        // Empty data
        final MockMultipartFile multipartFile = new MockMultipartFile("trackData", new byte[0]);
        mockMvc
            .perform(
                multipart("http://biking.michael-simons.eu/api/tracks")
                    .file(multipartFile)
                    .param("name", "poef")
                    .param("coveredOn", "2014-03-24T23:00:00.000Z")
                    .param("description", "description")
            )
            .andExpect(status().isBadRequest());

        // No data
        mockMvc
            .perform(
                multipart("http://biking.michael-simons.eu/api/tracks")
                    .param("name", "poef")
                    .param("coveredOn", "2014-03-24T23:00:00.000Z")
                    .param("description", "description")
            )
            .andExpect(status().isBadRequest());

        verifyNoInteractions(trackRepository);
    }

    @Test
    @WithMockUser
    void shouldHandleDataIntegrityViolationsGracefully() throws Exception {
        when(trackRepository.save(any(TrackEntity.class))).thenThrow(new DataIntegrityViolationException("fud"));

        MockMultipartFile trackData = new MockMultipartFile("trackData", this.getClass().getResourceAsStream("/test.tcx"));
        mockMvc
                .perform(
                        multipart("http://biking.michael-simons.eu/api/tracks")
                            .file(trackData)
                            .param("name", "name")
                            .param("coveredOn", "2014-01-01T21:21:21.000Z")
                            .param("description", "description")
                            .param("type", "biking")
                )
                .andExpect(status().isConflict());

        verify(trackRepository).save(Mockito.any(TrackEntity.class));
        verifyNoMoreInteractions(trackRepository);
    }

    @Test
    void shouldHandleIOExceptionsGracefully1() {
        TrackEntity track = Mockito.mock(TrackEntity.class);
        // return a directory so that the controller cannot write to it
        when(track.getTrackFile(this.datastoreBaseDirectory, "tcx")).thenReturn(this.datastoreBaseDirectory);

        final TrackRepository trackRepository = mock(TrackRepository.class);
        final TracksController controller = new TracksController(null, trackRepository, this.datastoreBaseDirectory, null, null);

        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy(() -> controller.storeFile(track, new ByteArrayInputStream(new byte[0])));

        verify(track).getTrackFile(this.datastoreBaseDirectory, "tcx");
        verify(track).getTrackFile(this.datastoreBaseDirectory, "gpx");
        verifyNoMoreInteractions(track);
    }

    @Test
    void shouldHandleIOExceptionsGracefully2() throws Exception {
        TrackEntity track = Mockito.mock(TrackEntity.class);
        when(track.getTrackFile(this.datastoreBaseDirectory, "tcx")).thenReturn(File.createTempFile("4223", ".tcx"));
        when(track.getTrackFile(this.datastoreBaseDirectory, "gpx")).thenReturn(File.createTempFile("4223", ".gpx"));

        final TrackRepository trackRepository = mock(TrackRepository.class);
        final TracksController controller = new TracksController(null, trackRepository, this.datastoreBaseDirectory, new File("/iam/not/gpsBabel").getAbsolutePath(), null);

        assertThatIOException()
                .isThrownBy(() -> controller.storeFile(track, new ByteArrayInputStream(new byte[0])))
                .withMessageMatching("Cannot run program \"/iam/not/gpsBabel\": error=2,.+");

        verify(track).getTrackFile(this.datastoreBaseDirectory, "tcx");
        verify(track).getTrackFile(this.datastoreBaseDirectory, "gpx");
        verifyNoMoreInteractions(track);
    }

    @Test
    @WithMockUser
    void shouldHandleInvalidTcxFiles() throws Exception {
        final TrackEntity track = Mockito.mock(TrackEntity.class);
        when(track.getTrackFile(this.datastoreBaseDirectory, "tcx")).thenReturn(File.createTempFile("4223", ".tcx"));
        when(track.getTrackFile(this.datastoreBaseDirectory, "gpx")).thenReturn(File.createTempFile("4223", ".gpx"));
        when(trackRepository.save(any(TrackEntity.class))).thenReturn(track);

        mockMvc
            .perform(
                multipart("http://biking.michael-simons.eu/api/tracks")
                    .file(new MockMultipartFile("trackData", this.getClass().getResourceAsStream("/test-invalid.tcx")))
                    .param("name", "name")
                    .param("coveredOn", "2014-01-01T21:21:21.000Z")
                    .param("description", "description")
                    .param("type", "biking")
            )
            .andExpect(status().isBadRequest());

        verify(track, times(2)).getTrackFile(this.datastoreBaseDirectory, "tcx");
        verify(track, times(2)).getTrackFile(this.datastoreBaseDirectory, "gpx");
        verifyNoMoreInteractions(track);
    }

    @Test
    void testGetHome() throws Exception {
        mockMvc.perform(get("http://biking.michael-simons.eu/api/home"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.longitude", is(home.getLongitude().doubleValue())))
            .andExpect(jsonPath("$.latitude", is(home.getLatitude().doubleValue())));
    }

    @TestConfiguration
    static class TracksControllerTestConfig implements EnvironmentAware {

        private ConfigurableEnvironment environment;

        @Bean
        public File datastoreBaseDirectory() {
            final File datastoreBaseDirectory  = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()));
            datastoreBaseDirectory.deleteOnExit();
            new File(datastoreBaseDirectory, DatastoreConfig.TRACK_DIRECTORY).mkdirs();
            return datastoreBaseDirectory;
        }

        @Bean
        public Coordinate home() {
            return new Coordinate(new BigDecimal("-122.41942"), new BigDecimal("37.77493"));
        }


        @Override
        public void setEnvironment(Environment environment) {
            this.environment = (ConfigurableEnvironment)environment;
        }

        @PostConstruct
        void configureGPSBabel() {
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
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(environment, "biking2.gpsBabel = " + tmp.getAbsolutePath());
        }
    }
}
