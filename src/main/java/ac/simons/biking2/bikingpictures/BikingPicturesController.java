/*
 * Copyright 2014-2019 michael-simons.eu.
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
import java.io.File;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Michael J. Simons
 * @since 2014-02-19
 */
@Controller
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class BikingPicturesController {

    private final BikingPictureRepository bikingPictureRepository;
    private final File datastoreBaseDirectory;

    @GetMapping("/api/bikingPictures")
    @ResponseBody
    public List<BikingPictureEntity> getBikingPictures() {
        return bikingPictureRepository.findAll(Sort.by("pubDate").ascending());
    }

    @GetMapping({"/api/bikingPictures/{id:\\d+}.jpg"})
    public void getBikingPicture(
            @PathVariable final Integer id,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {

        final BikingPictureEntity bikingPicture = this.bikingPictureRepository
                .findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        final FileBasedResource resource = new FileBasedResource(new File(datastoreBaseDirectory, String.format("%s/%d.jpg", DatastoreConfig.BIKING_PICTURES_DIRECTORY, bikingPicture.getExternalId())), String.format("%s.jpg", id), 365);
        resource.send(request, response);
    }
}
