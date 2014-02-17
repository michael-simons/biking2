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

import ac.simons.biking2.persistence.entities.Track;
import ac.simons.biking2.persistence.repositories.TrackRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael J. Simons, 2014-02-15
 */
@Controller
@RestController
public class TracksController {

    private final static Map<String, String> acceptableFormats;

    static {
	final Map<String, String> hlp = new HashMap<>();
	hlp.put("gpx", "application/gpx+xml");
	hlp.put("tcx", "application/xml");
	acceptableFormats = Collections.unmodifiableMap(hlp);
    }

    private final TrackRepository trackRepository;
    private final File datastoreBaseDirectory;

    @Autowired
    public TracksController(TrackRepository trackRepository, final File datastoreBaseDirectory) {
	this.trackRepository = trackRepository;
	this.datastoreBaseDirectory = datastoreBaseDirectory;
    }

    @RequestMapping("/api/tracks")
    public @ResponseBody
    List<Track> getTracks() {
	return trackRepository.findAll(new Sort(Sort.Direction.ASC, "coveredOn"));
    }

    @RequestMapping("/api/tracks/{id:\\w+}")
    public ResponseEntity<Track> getTrack(final @PathVariable String id) {
	final Integer _id = Track.getId(id);

	Track track;
	ResponseEntity<Track> rv;
	if (_id == null) {
	    rv = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
	} else if ((track = this.trackRepository.findOne(_id)) == null) {
	    rv = new ResponseEntity<>(HttpStatus.NOT_FOUND);
	} else {
	    rv = new ResponseEntity<>(track, HttpStatus.OK);
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
	final Integer _id = Track.getId(id);
	final String _format = Optional.ofNullable(format).orElse("").toLowerCase();
	Track track = null;
	if (_id == null || !acceptableFormats.containsKey(_format)) {
	    response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
	} else if ((track = this.trackRepository.findOne(_id)) == null) {
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	} else {
	    final File trackFile = new File(datastoreBaseDirectory, String.format("data/tracks/%d.%s", track.getId(), _format));
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
}
