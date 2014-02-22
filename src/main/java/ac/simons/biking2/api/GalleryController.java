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

import ac.simons.biking2.persistence.entities.GalleryPicture;
import ac.simons.biking2.persistence.repositories.GalleryPictureRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Michael J. Simons, 2014-02-22
 */
@Controller
public class GalleryController {

    private final GalleryPictureRepository galleryPictureRepository;
    private final Random random;

    @Autowired
    public GalleryController(GalleryPictureRepository galleryPictureRepository) {
	this.galleryPictureRepository = galleryPictureRepository;
	this.random = new Random(System.currentTimeMillis());
    }

    @RequestMapping("/api/galleryPictures/random")
    public @ResponseBody Collection<GalleryPicture> getRandomPictures(
	    final @RequestParam(value = "max", required = false, defaultValue = "10") int max
    ) {
	final List<GalleryPicture> allPictures = galleryPictureRepository.findAll();
	final GalleryPicture[] hlp = allPictures.toArray(new GalleryPicture[allPictures.size()]);
	
	for (int i = 0; i < hlp.length; ++i) {
	    final GalleryPicture galleryPicture = hlp[i];
	    int otherIndex = random.nextInt(hlp.length - i) + i;
	    hlp[i] = hlp[otherIndex];
	    hlp[otherIndex] = galleryPicture;
	}
	
	return Arrays.asList(hlp).subList(0, Math.min(max, hlp.length));	
    }
}
