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
package ac.simons.biking2.bikingpictures;

import ac.simons.biking2.config.DatastoreConfig;
import ac.simons.biking2.support.FileBasedResource;
import ac.simons.biking2.support.ResourceNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author Michael J. Simons, 2014-02-19
 */
@Controller
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class BikingPicturesController {

    private final BikingPictureRepository bikingPictureRepository;
    private final File datastoreBaseDirectory;

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

        final BikingPictureEntity bikingPicture = this.bikingPictureRepository
                .findById(id).orElseThrow(ResourceNotFoundException::new);

        final FileBasedResource resource = new FileBasedResource(new File(datastoreBaseDirectory, String.format("%s/%d.jpg", DatastoreConfig.BIKING_PICTURES_DIRECTORY, bikingPicture.getExternalId())), String.format("%s.jpg", id), 365);
        resource.send(request, response);
    }
}
