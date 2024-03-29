/*
 * Copyright 2014-2023 michael-simons.eu.
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
package ac.simons.biking2.gallerypictures;

import ac.simons.biking2.config.DatastoreConfig;
import ac.simons.biking2.support.FileBasedResource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static java.lang.String.format;
import java.nio.channels.ReadableByteChannel;
import static java.security.MessageDigest.getInstance;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Michael J. Simons, 2014-02-22
 */
@Controller
@RequestMapping("/api/galleryPictures")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class GalleryController {

    @FunctionalInterface
    public interface FilenameGenerator {

        String generateFile(String originalFilename);
    }

    private final GalleryPictureRepository galleryPictureRepository;
    private final File datastoreBaseDirectory;
    private final FilenameGenerator filenameGenerator = new FilenameGenerator() {
        @Override
        @SneakyThrows
        public String generateFile(final String originalFilename) {
            final byte[] digest = getInstance("MD5").digest(String.format("%s-%d", originalFilename, System.currentTimeMillis()).getBytes());
            return format("%032x.jpg", new BigInteger(1, digest));
        }
    };

    @GetMapping
    @ResponseBody
    public List<GalleryPictureEntity> getGalleryPictures() {
        return galleryPictureRepository.findAll(Sort.by("takenOn").ascending());
    }

    /**
     * Returns a subset of gallery pictures taken on a given day. The resulting list will be include all pictures on the
     * given date time truncated to the day up until the next day.
     *
     * @param takenOn The day the pictures should been taken on. It will be truncated to start of day.
     * @return A list of pictures taken on a specific day.
     */
    @GetMapping("/{takenOn}")
    @ResponseBody
    public List<GalleryPictureEntity> getGalleryPictures(
            @PathVariable
            @DateTimeFormat(iso = DATE)
            final LocalDate takenOn
    ) {
        return galleryPictureRepository.findAllByTakenOnBetween(takenOn, takenOn);
    }

    @PostMapping
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

            try (
                    ReadableByteChannel in = Channels.newChannel(imageData.getInputStream());
                    FileOutputStream out = new FileOutputStream(imageFile);
            ) {
                out.getChannel().transferFrom(in, 0, Integer.MAX_VALUE);
                out.flush();

                final GalleryPictureEntity galleryPicture = new GalleryPictureEntity(takenOn.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate(), filename);
                galleryPicture.setDescription(description);

                rv = new ResponseEntity<>(this.galleryPictureRepository.save(galleryPicture), HttpStatus.OK);
            } catch (IOException e) {
                log.error("Could not read or store image data, responding with an internal error!", e);
                rv = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (DataIntegrityViolationException e) {
                log.debug("Data integrity violation while uploading a new picture (filename=" + filename + ")", e);
                rv = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return rv;
    }

    @GetMapping("/{id:\\d+}.jpg")
    public void getGalleryPicture(
            @PathVariable final Integer id,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {

        final GalleryPictureEntity galleryPicture = this.galleryPictureRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        final FileBasedResource resource = new FileBasedResource(new File(datastoreBaseDirectory, String.format("%s/%s", DatastoreConfig.GALLERY_PICTURES_DIRECTORY, galleryPicture.getFilename())), String.format("%s.jpg", id), 365);
        resource.send(request, response);
    }
}
