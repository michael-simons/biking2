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

import ac.simons.biking2.tracks.gpx.GPX;
import ac.simons.biking2.support.JAXBContextFactory;
import ac.simons.biking2.support.ResourceNotFoundException;
import ac.simons.biking2.tracks.TrackEntity.Type;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Michael J. Simons, 2014-02-15
 */
@Controller
@Slf4j
class TracksController {

    private static final Map<String, String> ACCEPTABLE_FORMATS;

    static {
        final Map<String, String> hlp = new HashMap<>();
        hlp.put("gpx", "application/gpx+xml");
        hlp.put("tcx", "application/xml");
        ACCEPTABLE_FORMATS = Collections.unmodifiableMap(hlp);
    }

    private final TrackRepository trackRepository;
    private final File datastoreBaseDirectory;
    private final String gpsBabel;
    private final Coordinate home;
    private final JAXBContext gpxContext;

    TracksController(final TrackRepository trackRepository, final File datastoreBaseDirectory, @Value("${biking2.gpsBabel:/opt/local/bin/gpsbabel}") final String gpsBabel, final Coordinate home) {
        this.trackRepository = trackRepository;
        this.datastoreBaseDirectory = datastoreBaseDirectory;
        this.gpsBabel = gpsBabel;
        this.home = home;
        this.gpxContext = new JAXBContextFactory(GPX.class).createContext();
    }

    @RequestMapping("/api/tracks")
    @ResponseBody
    public
    List<TrackEntity> getTracks() {
        return trackRepository.findAll(new Sort(Sort.Direction.ASC, "coveredOn"));
    }

    @RequestMapping(value = "/api/tracks", method = POST)
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
                TrackEntity track = new TrackEntity(name, GregorianCalendar.from(coveredOn));
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
                    track.getTrackFile(datastoreBaseDirectory, "tcx").delete();
                    track.getTrackFile(datastoreBaseDirectory, "gpx").delete();

                    rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } catch (DataIntegrityViolationException e) {
                log.debug("Data integrity violation while storing a new track (coveredOn=" + coveredOn + ",name=" + name + ")", e);
                rv = new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        return rv;
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

    @RequestMapping(path = "/api/tracks/{id:\\w+}", method = RequestMethod.GET)
    @SuppressWarnings({"checkstyle:innerassignment"})
    public ResponseEntity<TrackEntity> getTrack(@PathVariable final String id) {
        final Integer requestedId = TrackEntity.getId(id);

        ResponseEntity<TrackEntity> rv;
        if (requestedId == null) {
            rv = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        return this.trackRepository
                .findById(requestedId)
                .map(track -> new ResponseEntity<>(track, HttpStatus.OK))
                .orElseThrow(ResourceNotFoundException::new);
    }

    @RequestMapping(path = "/api/tracks/{id:\\w+}", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated()")
    @SuppressWarnings({"checkstyle:innerassignment"})
    public ResponseEntity<Void> deleteTrack(@PathVariable final String id) {
        final Integer requestedId = TrackEntity.getId(id);


        ResponseEntity<Void> rv;
        if (requestedId == null) {
            rv = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } else {
            final TrackEntity track = this.trackRepository.findById(requestedId).orElseThrow(ResourceNotFoundException::new);
            final File tcxFile = track.getTrackFile(datastoreBaseDirectory, "tcx");
            final File gpxFile = track.getTrackFile(datastoreBaseDirectory, "gpx");
            if (tcxFile.delete() && gpxFile.delete()) {
                this.trackRepository.delete(track);
                rv = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                rv = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return rv;
    }

    @RequestMapping({"/tracks/{id:\\w+}.{format}"})
    @SuppressWarnings({"checkstyle:innerassignment"})
    public void downloadTrack(
            @PathVariable final String id,
            @PathVariable final String format,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        final Integer requestedId = TrackEntity.getId(id);
        final String requestedFormat = Optional.ofNullable(format).orElse("").toLowerCase();
        if (requestedId == null || !ACCEPTABLE_FORMATS.containsKey(requestedFormat)) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        } else {
            final TrackEntity track = this.trackRepository.findById(requestedId).orElseThrow(ResourceNotFoundException::new);
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

    @RequestMapping("/api/home")
    @ResponseBody
    public Coordinate getHome() {
        return this.home;
    }
}
