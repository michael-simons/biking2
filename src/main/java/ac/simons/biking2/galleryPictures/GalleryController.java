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
package ac.simons.biking2.galleryPictures;

import ac.simons.biking2.config.DatastoreConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static java.lang.String.format;
import static java.security.MessageDigest.getInstance;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael J. Simons, 2014-02-22
 */
@Controller
class GalleryController {

    public static final Logger LOGGER = LoggerFactory.getLogger(GalleryController.class.getPackage().getName());

    @FunctionalInterface
    public interface FilenameGenerator {

        String generateFile(final String originalFilename);
    }

    private final GalleryPictureRepository galleryPictureRepository;
    private final File datastoreBaseDirectory;
    private FilenameGenerator filenameGenerator = originalFilename -> {
        try {
            final byte[] digest = getInstance("MD5").digest(String.format("%s-%d", originalFilename, System.currentTimeMillis()).getBytes());
            return format("%032x.jpg", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    };

    GalleryController(final GalleryPictureRepository galleryPictureRepository, final File datastoreBaseDirectory) {
        this.galleryPictureRepository = galleryPictureRepository;
        this.datastoreBaseDirectory = datastoreBaseDirectory;
    }

    @RequestMapping("/api/galleryPictures")
    @ResponseBody
    public List<GalleryPictureEntity> getGalleryPictures() {
        return galleryPictureRepository.findAll(new Sort(Sort.Direction.ASC, "takenOn"));
    }

    @RequestMapping(value = "/api/galleryPictures", method = POST)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GalleryPictureEntity> createGalleryPicture(
            @RequestParam(value = "takenOn", required = true)
            @DateTimeFormat(iso = DATE_TIME)
            final ZonedDateTime takenOn,
            @RequestParam(value = "description", required = true)
            final String description,
            @RequestParam("imageData")
            final MultipartFile imageData
    ) {
        ResponseEntity<GalleryPictureEntity> rv;
        if (imageData == null || imageData.isEmpty()) {
            rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            final String filename = this.filenameGenerator.generateFile(imageData.getOriginalFilename());
            final File imageFile = new File(datastoreBaseDirectory, String.format("%s/%s", DatastoreConfig.GALLERY_PICTURES_DIRECTORY, filename));

            try (FileOutputStream out = new FileOutputStream(imageFile);) {
                out.getChannel().transferFrom(Channels.newChannel(imageData.getInputStream()), 0, Integer.MAX_VALUE);
                out.flush();

                GalleryPictureEntity galleryPicture = new GalleryPictureEntity(GregorianCalendar.from(takenOn), filename);
                galleryPicture.setDescription(description);

                rv = new ResponseEntity<>(this.galleryPictureRepository.save(galleryPicture), HttpStatus.OK);
            } catch (IOException e) {
                LOGGER.error("Could not read or store image data, responding with an internal error!", e);
                rv = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (DataIntegrityViolationException e) {
                LOGGER.debug("Data integrity violation while uploading a new picture (filename=" + filename + ")", e);
                rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return rv;
    }

    @RequestMapping({"/api/galleryPictures/{id:\\d+}.jpg"})
    public void getGalleryPicture(
            @PathVariable final Integer id,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {

        final GalleryPictureEntity galleryPicture = this.galleryPictureRepository.findOne(id);
        if (galleryPicture == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final File imageFile = new File(datastoreBaseDirectory, String.format("%s/%s", DatastoreConfig.GALLERY_PICTURES_DIRECTORY, galleryPicture.getFilename()));

            final int cacheForDays = 365;
            response.setHeader("Content-Type", "image/jpeg");
            response.setHeader("Content-Disposition", String.format("inline; filename=\"%s.jpg\"", id));
            response.setHeader("Expires", now(of("UTC")).plusDays(cacheForDays).format(RFC_1123_DATE_TIME));
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
