/*
 * Copyright 2014-2023 michael-simons.eu.
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

import ac.simons.biking2.support.JAXBContextFactory;
import ac.simons.biking2.tracks.TrackEntity.Type;
import ac.simons.biking2.tracks.gpx.GPX;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

/**
 * @author Michael J. Simons
 * @since 2014-02-15
 */
@Controller
@Slf4j
class TracksController {

    private static final Map<String, String> ACCEPTABLE_FORMATS = Map.of("gpx", "application/gpx+xml", "tcx", "application/xml");

    private final TrackIdParser trackIdParser;
    private final TrackRepository trackRepository;
    private final File datastoreBaseDirectory;
    private final String gpsBabel;
    private final Coordinate home;
    private final JAXBContext gpxContext;

    TracksController(final TrackIdParser trackIdParser, final TrackRepository trackRepository, final File datastoreBaseDirectory, @Value("${biking2.gpsBabel:/opt/local/bin/gpsbabel}") final String gpsBabel, final Coordinate home) {
        this.trackIdParser = trackIdParser;
        this.trackRepository = trackRepository;
        this.datastoreBaseDirectory = datastoreBaseDirectory;
        this.gpsBabel = gpsBabel;
        this.home = home;
        this.gpxContext = new JAXBContextFactory(GPX.class).createContext();
    }

    @GetMapping("/api/tracks")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public
    List<TrackEntity> getTracks() {
        return trackRepository.findAll(Sort.by("coveredOn").ascending());
    }

    @PostMapping(value = "/api/tracks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TrackEntity> createTrack(
            @RequestParam(value = "name", required = true)
            final String name,
            @RequestParam(value = "coveredOn", required = true)
            @DateTimeFormat(iso = DATE_TIME)
            final ZonedDateTime coveredOn,
            @RequestParam(value = "description", required = false)
            final String description,
            @RequestParam(value = "type", required = true, defaultValue = "biking")
            final Type type,
            @RequestParam("trackData")
            final MultipartFile trackData
    ) throws IOException {
        ResponseEntity<TrackEntity> rv;
        if (trackData == null || trackData.isEmpty()) {
            rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            try {
                TrackEntity track = new TrackEntity(name, coveredOn.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate());
                track.setDescription(description);
                track.setType(type);

                track = this.trackRepository.save(track);

                try {
                    this.storeFile(track, trackData.getInputStream());

                    track = this.trackRepository.save(track);
                    rv = new ResponseEntity<>(track, HttpStatus.OK);
                } catch (Exception e) {
                    log.warn("Could not store track... Maybe an invalid GPX file? Handling as a bad request.", e);

                    this.trackRepository.delete(track);
                    deleteTrackFile(track.getTrackFile(datastoreBaseDirectory, "tcx"));
                    deleteTrackFile(track.getTrackFile(datastoreBaseDirectory, "gpx"));

                    rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } catch (DataIntegrityViolationException e) {
                log.debug("Data integrity violation while storing a new track (coveredOn=" + coveredOn + ",name=" + name + ")", e);
                rv = new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        return rv;
    }

    private static void deleteTrackFile(final File trackFile) {

        if (trackFile != null && trackFile.isFile() && !trackFile.delete()) {
            log.warn("Could not delete tcx file {} during storage exception.", trackFile.getAbsolutePath());
        }
    }

    @SneakyThrows({IOException.class, JAXBException.class, InterruptedException.class})
    TrackEntity storeFile(final TrackEntity track, final InputStream tcxData) {
        final File tcxFile = track.getTrackFile(datastoreBaseDirectory, "tcx");
        final File gpxFile = track.getTrackFile(datastoreBaseDirectory, "gpx");

        try (
                ReadableByteChannel tcxDataChannel = Channels.newChannel(tcxData);
                FileOutputStream out = new FileOutputStream(tcxFile);
        ) {
            out.getChannel().transferFrom(tcxDataChannel, 0, Integer.MAX_VALUE);
            out.flush();
        }

        final Process process = new ProcessBuilder(gpsBabel, "-i", "gtrnctr", "-f", tcxFile.getAbsolutePath(), "-o", "gpx", "-F", gpxFile.getAbsolutePath()).start();
        process.waitFor();
        int exitValue = process.exitValue();

        if (exitValue != 0) {
            throw new GPSBabelException("GPSBabel could not convert the input file!");
        }
        final Unmarshaller unmarschaller = gpxContext.createUnmarshaller();
        GPX gpx = (GPX) unmarschaller.unmarshal(gpxFile);
        track.setMinlon(gpx.getBounds().getMinlon());
        track.setMinlat(gpx.getBounds().getMinlat());
        track.setMaxlon(gpx.getBounds().getMaxlon());
        track.setMaxlat(gpx.getBounds().getMaxlat());

        return track;
    }

    @GetMapping(path = "/api/tracks/{id:\\w+}")
    @SuppressWarnings({"checkstyle:innerassignment"})
    public ResponseEntity<TrackEntity> getTrack(@PathVariable final String id) {
        final Integer requestedId = trackIdParser.fromPrettyId(id);

        return requestedId == null ? new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE) : this.trackRepository
                .findById(requestedId)
                .map(track -> new ResponseEntity<>(track, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/api/tracks/{id:\\w+}")
    @PreAuthorize("isAuthenticated()")
    @SuppressWarnings({"checkstyle:innerassignment"})
    public ResponseEntity<Void> deleteTrack(@PathVariable final String id) {
        final Integer requestedId = trackIdParser.fromPrettyId(id);

        ResponseEntity<Void> rv;
        if (requestedId == null) {
            rv = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } else {
            final TrackEntity track = this.trackRepository.findById(requestedId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            final File tcxFile = track.getTrackFile(datastoreBaseDirectory, "tcx");
            final File gpxFile = track.getTrackFile(datastoreBaseDirectory, "gpx");
            if (tcxFile.delete() && gpxFile.delete()) {
                log.debug("Deleted {} and {}", tcxFile, gpxFile);
                this.trackRepository.delete(track);
                rv = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                rv = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return rv;
    }

    @GetMapping({"/tracks/{id:\\w+}.{format}"})
    @SuppressWarnings({"checkstyle:innerassignment"})
    public void downloadTrack(
            @PathVariable final String id,
            @PathVariable final String format,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        final Integer requestedId = trackIdParser.fromPrettyId(id);
        final String requestedFormat = Optional.ofNullable(format).orElse("").toLowerCase();
        if (requestedId == null || !ACCEPTABLE_FORMATS.containsKey(requestedFormat)) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        } else {
            final TrackEntity track = this.trackRepository.findById(requestedId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            final File trackFile = track.getTrackFile(datastoreBaseDirectory, requestedFormat);
            response.setHeader("Content-Type", ACCEPTABLE_FORMATS.get(requestedFormat));
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.%s\"", id, requestedFormat));

            // Attribute maybe null
            if (request == null || !Boolean.TRUE.equals(request.getAttribute("org.apache.tomcat.sendfile.support"))) {
                Files.copy(trackFile.toPath(), response.getOutputStream());
                response.getOutputStream().flush();
            } else {
                long l = trackFile.length();
                request.setAttribute("org.apache.tomcat.sendfile.filename", trackFile.getAbsolutePath());
                request.setAttribute("org.apache.tomcat.sendfile.start", 0L);
                request.setAttribute("org.apache.tomcat.sendfile.end", l);
                response.setHeader("Content-Length", Long.toString(l));
            }
        }

        response.flushBuffer();
    }

    @GetMapping("/api/home")
    @ResponseBody
    public Coordinate getHome() {
        return this.home;
    }
}
