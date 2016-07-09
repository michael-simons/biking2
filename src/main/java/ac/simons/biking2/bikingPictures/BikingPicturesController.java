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
package ac.simons.biking2.bikingPictures;

import ac.simons.biking2.config.DatastoreConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;

/**
 * @author Michael J. Simons, 2014-02-19
 */
@Controller
class BikingPicturesController {

    private final BikingPictureRepository bikingPictureRepository;
    private final File datastoreBaseDirectory;

    public BikingPicturesController(BikingPictureRepository bikingPictureRepository, final File datastoreBaseDirectory) {
	this.bikingPictureRepository = bikingPictureRepository;
	this.datastoreBaseDirectory = datastoreBaseDirectory;
    }

    @RequestMapping("/api/bikingPictures")
    @ResponseBody
    public List<BikingPictureEntity> getBikingPictures() {
	return bikingPictureRepository.findAll(new Sort(Sort.Direction.ASC, "pubDate"));
    }

    @RequestMapping({"/api/bikingPictures/{id:\\d+}.jpg"})
    public void getBikingPicture(
	    @PathVariable final Integer id,
	    final HttpServletRequest request,
	    final HttpServletResponse response
    ) throws IOException {

	BikingPictureEntity bikingPicture;
	if ((bikingPicture = this.bikingPictureRepository.findOne(id)) == null) {
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	} else {
	    final File imageFile = new File(datastoreBaseDirectory, String.format("%s/%d.jpg", DatastoreConfig.BIKING_PICTURES_DIRECTORY, bikingPicture.getExternalId()));

	    final int cacheForDays = 365;
	    response.setHeader("Content-Type", "image/jpeg");
	    response.setHeader("Content-Disposition", String.format("inline; filename=\"%s.jpg\"", id));
	    response.setHeader("Expires", now(of("UTC")).plusDays(cacheForDays).format(RFC_1123_DATE_TIME.withLocale(Locale.US)));
	    response.setHeader("Cache-Control", String.format("max-age=%d, %s", TimeUnit.DAYS.toSeconds(cacheForDays), "public"));

	    // Attribute maybe null
	    if (request == null || !Boolean.TRUE.equals(request.getAttribute("org.apache.tomcat.sendfile.support"))) {
		Files.copy(imageFile.toPath(), response.getOutputStream());
		response.getOutputStream().flush();
	    } else {
		long l = imageFile.length();
		request.setAttribute("org.apache.tomcat.sendfile.filename", imageFile.getAbsolutePath());
		request.setAttribute("org.apache.tomcat.sendfile.start", 0L);
		request.setAttribute("org.apache.tomcat.sendfile.end", l);
		response.setHeader("Content-Length", Long.toString(l));
	    }
	}

	response.flushBuffer();
    }
}
