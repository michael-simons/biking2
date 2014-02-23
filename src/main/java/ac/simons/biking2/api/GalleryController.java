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
import ac.simons.biking2.persistence.entities.GalleryPicture;
import ac.simons.biking2.persistence.entities.Track;
import ac.simons.biking2.persistence.entities.Track.Type;
import ac.simons.biking2.persistence.repositories.GalleryPictureRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import org.apache.catalina.util.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import static java.lang.String.format;
import static org.apache.catalina.util.MD5Encoder.encode;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Michael J. Simons, 2014-02-22
 */
@Controller
public class GalleryController {

    @FunctionalInterface
    public interface FilenameGenerator {
	public String generateFile(final String originalFilename);
    }
    
    private final GalleryPictureRepository galleryPictureRepository;
    private final File datastoreBaseDirectory;
    private final Random random;
    private FilenameGenerator filenameGenerator = originalFilename -> {
	return format("%s.jpg", encode(String.format("%s-%d", originalFilename, System.currentTimeMillis()).getBytes()));	    
    };
    
    @Autowired
    public GalleryController(GalleryPictureRepository galleryPictureRepository, final File datastoreBaseDirectory) {
	this.galleryPictureRepository = galleryPictureRepository;
	this.datastoreBaseDirectory = datastoreBaseDirectory;
	this.random = new Random(System.currentTimeMillis());
    }

    public void setFilenameGenerator(FilenameGenerator filenameGenerator) {
	this.filenameGenerator = filenameGenerator;
    }

    @RequestMapping("/api/galleryPictures")
    public @ResponseBody
    List<GalleryPicture> getBikingPictures() {
	return galleryPictureRepository.findAll(new Sort(Sort.Direction.ASC, "takenOn"));
    }
    
    @RequestMapping(value = "/api/galleryPictures", method = POST)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GalleryPicture> createTrack(	    
	    @RequestParam(value = "takenOn", required = true)
	    @DateTimeFormat(iso = DATE_TIME) 
	    final ZonedDateTime takenOn,
	    @RequestParam(value = "description", required = true)
	    final String description,
	    @RequestParam("imageData")
	    final MultipartFile imageData
    ) {
	ResponseEntity<GalleryPicture> rv;
	if(imageData == null || imageData.isEmpty())
	    rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	else {
	    final String filename = this.filenameGenerator.generateFile(imageData.getOriginalFilename());
	    final File imageFile = new File(datastoreBaseDirectory, String.format("%s/%d", PersistenceConfig.TRACK_DIRECTORY, filename));
	    
	    try (FileOutputStream out = new FileOutputStream(imageFile);) {
		out.getChannel().transferFrom(Channels.newChannel(imageData.getInputStream()), 0, Integer.MAX_VALUE);
		out.flush();
		
		GalleryPicture galleryPicture = new GalleryPicture(GregorianCalendar.from(takenOn), filename);
		galleryPicture.setDescription(description);
		
		rv = new ResponseEntity<>(this.galleryPictureRepository.save(galleryPicture), HttpStatus.OK);
	    } catch(IOException e) {		
		// Could not store data...
		rv = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);	
	    } catch(DataIntegrityViolationException e) {
		rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);	
	    }		    	    
	}
	return rv;
    }
}
