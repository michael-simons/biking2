/*
 * Copyright 2014-2016 Michael J. Simons.
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
import ac.simons.biking2.tracks.TrackEntity.Type;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
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
class TracksController {

    private static final Map<String, String> acceptableFormats;

    static {
        final Map<String, String> hlp = new HashMap<>();
        hlp.put("gpx", "application/gpx+xml");
        hlp.put("tcx", "application/xml");
        acceptableFormats = Collections.unmodifiableMap(hlp);
    }

    private final TrackRepository trackRepository;
    private final File datastoreBaseDirectory;
    private final String gpsBabel;
    private final Coordinate home;
    private final JAXBContext gpxContext;

    public TracksController(final TrackRepository trackRepository, final File datastoreBaseDirectory, @Value("${biking2.gpsBabel:/opt/local/bin/gpsbabel}") final String gpsBabel, final Coordinate home) {
        this.trackRepository = trackRepository;
        this.datastoreBaseDirectory = datastoreBaseDirectory;
        this.gpsBabel = gpsBabel;
        this.home = home;
        this.gpxContext = JAXBContextFactory.createContext(GPX.class);
    }

    @RequestMapping("/api/tracks")
    public @ResponseBody
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
        if(trackData == null || trackData.isEmpty())
            rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        else {
            try {
                TrackEntity track = new TrackEntity(name, GregorianCalendar.from(coveredOn));
                track.setDescription(description);
                track.setType(type);

                track = this.trackRepository.save(track);

                try {
                    this.storeFile(track, trackData.getInputStream());

                    track = this.trackRepository.save(track);
                    rv = new ResponseEntity<>(track, HttpStatus.OK);
                } catch(Exception e) {
                    this.trackRepository.delete(track);
                    track.getTrackFile(datastoreBaseDirectory, "tcx").delete();
                    track.getTrackFile(datastoreBaseDirectory, "gpx").delete();

                    rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } catch(DataIntegrityViolationException e) {
                rv = new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        return rv;
    }

    TrackEntity storeFile(final TrackEntity track, final InputStream tcxData) {
        final File tcxFile = track.getTrackFile(datastoreBaseDirectory, "tcx");
        final File gpxFile = track.getTrackFile(datastoreBaseDirectory, "gpx");

        try (FileOutputStream out = new FileOutputStream(tcxFile);) {
            out.getChannel().transferFrom(Channels.newChannel(tcxData), 0, Integer.MAX_VALUE);
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try {
            final Process process = new ProcessBuilder(gpsBabel, "-i", "gtrnctr", "-f", tcxFile.getAbsolutePath(), "-o", "gpx", "-F", gpxFile.getAbsolutePath()).start();
            process.waitFor();
            int exitValue = process.exitValue();

            if(exitValue != 0)
                throw new RuntimeException("GPSBabel could not convert the input file!");
            final Unmarshaller unmarschaller = gpxContext.createUnmarshaller();
            GPX gpx = (GPX) unmarschaller.unmarshal(gpxFile);
            track.setMinlon(gpx.getBounds().getMinlon());
            track.setMinlat(gpx.getBounds().getMinlat());
            track.setMaxlon(gpx.getBounds().getMaxlon());
            track.setMaxlat(gpx.getBounds().getMaxlat());
        } catch (IOException | JAXBException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        return track;
    }

    @RequestMapping(path = "/api/tracks/{id:\\w+}", method = RequestMethod.GET)
    public ResponseEntity<TrackEntity> getTrack(final @PathVariable String id) {
        final Integer _id = TrackEntity.getId(id);

        TrackEntity track;
        ResponseEntity<TrackEntity> rv;
        if (_id == null) {
            rv = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } else if ((track = this.trackRepository.findOne(_id)) == null) {
            rv = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            rv = new ResponseEntity<>(track, HttpStatus.OK);
        }

        return rv;
    }

    @RequestMapping(path = "/api/tracks/{id:\\w+}", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTrack(final @PathVariable String id) {
        final Integer _id = TrackEntity.getId(id);

        TrackEntity track;
        ResponseEntity<Void> rv;
        if (_id == null) {
            rv = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } else if ((track = this.trackRepository.findOne(_id)) == null) {
            rv = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
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
    public void downloadTrack(
            final @PathVariable String id,
            final @PathVariable String format,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        final Integer _id = TrackEntity.getId(id);
        final String _format = Optional.ofNullable(format).orElse("").toLowerCase();
        TrackEntity track;
        if (_id == null || !acceptableFormats.containsKey(_format)) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        } else if ((track = this.trackRepository.findOne(_id)) == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final File trackFile = track.getTrackFile(datastoreBaseDirectory, _format);
            response.setHeader("Content-Type", acceptableFormats.get(_format));
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.%s\"", id, _format));

            // Attribute maybe null
            if (request == null || !Boolean.TRUE.equals(request.getAttribute("org.apache.tomcat.sendfile.support"))) {
                Files.copy(trackFile.toPath(), response.getOutputStream());
                response.getOutputStream().flush();
            } else {
                long l = trackFile.length();
                request.setAttribute("org.apache.tomcat.sendfile.filename", trackFile.getAbsolutePath());
                request.setAttribute("org.apache.tomcat.sendfile.start", 0l);
                request.setAttribute("org.apache.tomcat.sendfile.end", l);
                response.setHeader("Content-Length", Long.toString(l));
            }
        }

        response.flushBuffer();
    }

    @RequestMapping("/api/home")
    public @ResponseBody Coordinate getHome() {
        return this.home;
    }
}
