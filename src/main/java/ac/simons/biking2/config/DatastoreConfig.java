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
package ac.simons.biking2.config;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Configuration
public class DatastoreConfig {

    public static final String BIKING_PICTURES_DIRECTORY = "data/bikingPictures";
    public static final String GALLERY_PICTURES_DIRECTORY = "data/galleryPictures";
    public static final String TRACK_DIRECTORY = "data/tracks";

    /**
     * Configures a file based datastore for storing large objects (tracks and
     * biking pictures)
     *
     * @param datastoreBaseDirectoryPath
     * @return
     */
    @Bean
    public File datastoreBaseDirectory(@Value("${biking2.datastore-base-directory:${user.dir}/var/dev}") final String datastoreBaseDirectoryPath) {
        final File rv = new File(datastoreBaseDirectoryPath);
        if (!(rv.isDirectory() || rv.mkdirs())) {
            throw new RuntimeException(String.format("Could not initialize '%s' as base directory for datastore!", rv.getAbsolutePath()));
        }

        new File(rv, BIKING_PICTURES_DIRECTORY).mkdirs();
        new File(rv, GALLERY_PICTURES_DIRECTORY).mkdirs();
        new File(rv, TRACK_DIRECTORY).mkdirs();
        return rv;
    }
}
