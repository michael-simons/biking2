/*
 * Copyright 2014-2016 michael-simons.eu.
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

import ac.simons.biking2.support.ResourceNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.URL;
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
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class OEmbedController {

    private static final Pattern EMBEDDABLE_TRACK_URL_PATTERN = Pattern.compile(".*?\\/tracks\\/(\\w+)(\\/|\\.(\\w+))?$");
    private static final Map<String, String> ACCEPTABLE_FORMATS;

    static {
        final Map<String, String> hlp = new HashMap<>();
        hlp.put("json", "application/json");
        hlp.put("xml", "application/xml");
        ACCEPTABLE_FORMATS = Collections.unmodifiableMap(hlp);
    }

    private final TrackIdParser trackIdParser;
    private final TrackRepository trackRepository;
    private final Coordinate home;

    @RequestMapping(value = "/oembed", produces = {"application/json", "application/xml"})
    @SuppressWarnings({"checkstyle:innerassignment"})
    public ResponseEntity<OEmbedResponse> getEmbeddableTrack(
            @RequestParam(required = true) @URL final String url,
            @RequestParam(required = false, defaultValue = "json") final String format,
            @RequestParam(required = false, defaultValue = "1024") final Integer maxwidth,
            @RequestParam(required = false, defaultValue = "576") final Integer maxheight,
            final HttpServletRequest request
    ) {
        ResponseEntity<OEmbedResponse> rv;
        final Matcher m = EMBEDDABLE_TRACK_URL_PATTERN.matcher(url);
        final Integer id = m.matches() ? trackIdParser.fromPrettyId(m.group(1)) : null;
        final String requestedFormat = Optional.ofNullable(format).orElse("").toLowerCase();
        if (id == null || !ACCEPTABLE_FORMATS.containsKey(requestedFormat)) {
            rv = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } else {
            final TrackEntity track = this.trackRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
            final OEmbedResponse response = new OEmbedResponse();

            response.setType("rich");
            response.setVersion("1.0");
            response.setTitle(track.getName());
            response.setAuthorName("Michael J. Simons");
            response.setAuthorUrl("http://michael-simons.eu");
            response.setProviderName("biking2");
            response.setProviderUrl("http://biking.michael-simons.eu");
            response.setCacheAge((long) (24 * 60 * 60));
            response.setHtml(new StringBuilder()
                    .append("<iframe ")
                    .append("width='").append(maxwidth).append("' ")
                    .append("height='").append(maxheight).append("' ")
                    .append("src='")
                    .append(request.getScheme()).append("://")
                    .append(request.getServerName())
                    .append(Arrays.asList(80, 443).contains(request.getServerPort()) ? "" : (":" + request.getServerPort()))
                    .append(request.getContextPath())
                    .append("/tracks/").append(m.group(1)).append("/embed?")
                    .append("width=").append(maxwidth).append("&")
                    .append("height=").append(maxheight)
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
    @SuppressWarnings({"checkstyle:innerassignment"})
    public String embedTrack(
            @PathVariable final String id,
            @RequestParam(required = false, defaultValue = "1024") final Integer width,
            @RequestParam(required = false, defaultValue = "576") final Integer height,
            final Model model,
            final HttpServletResponse response
    ) {
        final Integer requestedId = trackIdParser.fromPrettyId(id);
        TrackEntity track;
        String rv = null;
        if (requestedId == null) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        } else if ((track = this.trackRepository.findById(requestedId).orElse(null)) == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            model
                    .addAttribute("track", track)
                    .addAttribute("home", home)
                    .addAttribute("width", width)
                    .addAttribute("height", height);
            rv = "oEmbed/embeddedTrack";
        }
        return rv;
    }
}
