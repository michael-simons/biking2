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
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Configuration
@Profile({"dev", "prod"})
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(final Environment environment) {
	final JdbcDataSource rv = new JdbcDataSource();

	final String defaultDatabaseFile = new File(System.getProperty("user.dir"), String.format("biking-%s", environment.acceptsProfiles("dev") ? "dev" : "prod")).getAbsolutePath();
	rv.setUrl(String.format("jdbc:h2:file:%s;FILE_LOCK=FS", environment.getProperty("biking.database-file", defaultDatabaseFile)));
	rv.setUser("biking2");
	rv.setPassword("biking2");

	return rv;
    }
}
