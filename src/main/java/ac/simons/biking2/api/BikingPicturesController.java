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

import ac.simons.biking2.config.PersistenceConfig;
import ac.simons.biking2.persistence.entities.BikingPicture;
import ac.simons.biking2.persistence.repositories.BikingPictureRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Michael J. Simons, 2014-02-19
 */
@Controller
public class BikingPicturesController {

    private final BikingPictureRepository bikingPictureRepository;
    private final File datastoreBaseDirectory;

    @Autowired
    public BikingPicturesController(BikingPictureRepository bikingPictureRepository, final File datastoreBaseDirectory) {
	this.bikingPictureRepository = bikingPictureRepository;
	this.datastoreBaseDirectory = datastoreBaseDirectory;
    }

    @RequestMapping("/api/bikingPictures")
    public @ResponseBody
    List<BikingPicture> getBikingPictures() {
	return bikingPictureRepository.findAll(new Sort(Sort.Direction.ASC, "pubDate"));
    }

    @RequestMapping({"/api/bikingPictures/{id:\\d+}.jpg"})
    public void getBikingPicture(
	    final @PathVariable Integer id,
	    final HttpServletRequest request,
	    final HttpServletResponse response
    ) throws IOException {

	BikingPicture bikingPicture;
	if ((bikingPicture = this.bikingPictureRepository.findOne(id)) == null) {
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	} else {
	    final File imageFile = new File(datastoreBaseDirectory, String.format("%s/%d.jpg", PersistenceConfig.BIKING_PICTURES_DIRECTORY, bikingPicture.getExternalId()));
	    response.setHeader("Content-Type", "image/jpeg");
	    response.setHeader("Content-Disposition", String.format("inline; filename=\"%s.jpg\"", id));

	    // Attribute maybe null
	    if (request == null || !Boolean.TRUE.equals(request.getAttribute("org.apache.tomcat.sendfile.support"))) {
		Files.copy(imageFile.toPath(), response.getOutputStream());
		response.getOutputStream().flush();
	    } else {
		long l = imageFile.length();
		request.setAttribute("org.apache.tomcat.sendfile.filename", imageFile.getAbsolutePath());
		request.setAttribute("org.apache.tomcat.sendfile.start", 0l);
		request.setAttribute("org.apache.tomcat.sendfile.end", l);
		response.setHeader("Content-Length", Long.toString(l));
	    }
	}

	response.flushBuffer();
    }
}
