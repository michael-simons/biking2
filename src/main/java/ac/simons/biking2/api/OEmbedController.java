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
import ac.simons.biking2.api.model.OEmbedResponse;
import ac.simons.biking2.persistence.entities.Track;
import ac.simons.biking2.persistence.repositories.TrackRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Michael J. Simons, 2014-02-14
 */
@Controller
public class OEmbedController {
    private final static Pattern EMBEDDABLE_TRACK_URL_PATTERN = Pattern.compile(".*?\\/tracks\\/(\\w+)(\\/|\\.(\\w+))?$");
    private final static Map<String, String> acceptableFormats;
    static {
	final Map<String, String> hlp = new HashMap<>();
	hlp.put("json", "application/json");
	hlp.put("xml", "application/xml");
	acceptableFormats = Collections.unmodifiableMap(hlp);
    }
    
    private final TrackRepository trackRepository;
    private final Coordinate home;
    
    @Autowired
    public OEmbedController(TrackRepository TrackRepository, final Coordinate home) {
	this.trackRepository = TrackRepository;
	this.home = home;
    }
    
    @RequestMapping(value = "/oembed", produces = {"application/json", "application/xml"})
    public ResponseEntity<OEmbedResponse> getEmbeddableTrack(
	    final @RequestParam(required = true) @URL String url, 
	    final @RequestParam(required = false, defaultValue = "json") String format,
	    final @RequestParam(required = false, defaultValue = "1024") Integer width,
	    final @RequestParam(required = false, defaultValue = "576") Integer height,
	    final HttpServletRequest request
    ) {
	ResponseEntity<OEmbedResponse> rv = null;
	final Matcher m = EMBEDDABLE_TRACK_URL_PATTERN.matcher(url);
	final Integer id = m.matches() ? Track.getId(m.group(1)) : null;
	final String _format = Optional.ofNullable(format).orElse("").toLowerCase();
	Track track;
	if(id == null || !acceptableFormats.containsKey(_format))
	    rv = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
	else if((track = this.trackRepository.findOne(id)) == null)
	    rv = new ResponseEntity<>(HttpStatus.NOT_FOUND);
	else {
	    final OEmbedResponse response = new OEmbedResponse();
	    
	    response.setType("rich");
	    response.setVersion("1.0");
	    response.setTitle(track.getName());
	    response.setAuthorName("Michael J. Simons");
	    response.setAuthorUrl("http://michael-simons.eu");
	    response.setProviderName("biking2");
	    response.setProviderUrl("http://biking.michael-simons.eu");
	    response.setCacheAge((long)(24 * 60 * 60));
	    response.setHtml(new StringBuilder()
		    .append("<iframe ")
			.append("width='").append(width).append("' ")
			.append("height='").append(height).append("' ")
			.append("src='")
			    .append(request.getScheme()).append("://")
			    .append(request.getServerName())
			    .append(Arrays.asList(80, 443).contains(request.getServerPort()) ? "" : (":" + request.getServerPort()))
			    .append(request.getContextPath())
			    .append("/tracks/").append(m.group(1)).append("/embed?")
				.append("width=").append(width).append("&")
				.append("height=").append(height)		    
			.append("' ")
			.append("class='bikingTrack'>")
		    .append("</iframe>")		    
		    .toString()
	    );
	    
	    rv = new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	return rv;
    }

    @RequestMapping(value = "/tracks/{id:\\w+}/embed")
    public String embedTrack(
	    final @PathVariable String id,
	    final @RequestParam(required = false, defaultValue = "1024") Integer width,
	    final @RequestParam(required = false, defaultValue = "576") Integer height,
	    final Model model,
	    final HttpServletResponse response
    ) {	
	final Integer _id = Track.getId(id);	
	Track track;
	if (_id == null) {
	    response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
	} else if ((track = this.trackRepository.findOne(_id)) == null) {
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	} else {
	    model
		    .addAttribute("track", track)
		    .addAttribute("home", home)
		    .addAttribute("width", width)
		    .addAttribute("height", height);
	}
	return "/WEB-INF/views/oEmbed/embeddedTrack.jspx";
    }
}
