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

package ac.simons.biking2.config;

import java.io.File;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * @author Michael J. Simons, 2014-02-15
 */
@Configuration
@Profile({"dev", "prod"})
public class DataStorageConfig {
    @Bean
    public File datastoreBaseDirectory(final Environment environment) {
	final String customDatastoreBaseDirectory = environment.getProperty("biking2.datastore-base-directory");
	File rv;
	if(customDatastoreBaseDirectory == null || customDatastoreBaseDirectory.isEmpty())
	    rv = new File(System.getProperty("user.dir"), String.format("var/%s", environment.acceptsProfiles("dev") ? "dev" : "prod"));
	else
	    rv = new File(customDatastoreBaseDirectory);
	
	if(!(rv.isDirectory() || rv.mkdir()))
	    throw new RuntimeException(String.format("Could not initialize '%s' as base directory for datastore!", rv.getAbsolutePath()));
	
	new File(rv, "data/bikingPictures").mkdirs();
	new File(rv, "data/galleryPictures").mkdirs();
	new File(rv, "data/tracks").mkdirs();
		
	return rv;
    } 
}
