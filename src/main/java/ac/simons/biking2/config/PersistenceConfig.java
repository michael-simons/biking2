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
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.dialect.H2Dialect;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Configuration
@EnableJpaRepositories(basePackages = "ac.simons.biking2.persistence.repositories")
@EnableTransactionManagement
public class PersistenceConfig {

    public static final String BIKING_PICTURES_DIRECTORY = "data/bikingPictures";
    public static final String TRACK_DIRECTORY = "data/tracks";

    /**
     * Configures a file based datastore for storing large objects (tracks and
     * biking pictures)
     *
     * @param datastoreBaseDirectoryPath
     * @return
     */
    @Bean
    public File datastoreBaseDirectory(final @Value("${biking2.datastore-base-directory:${user.dir}/var/dev}") String datastoreBaseDirectoryPath) {
	final File rv = new File(datastoreBaseDirectoryPath);
	if (!(rv.isDirectory() || rv.mkdirs())) {
	    throw new RuntimeException(String.format("Could not initialize '%s' as base directory for datastore!", rv.getAbsolutePath()));
	}

	new File(rv, BIKING_PICTURES_DIRECTORY).mkdirs();
	new File(rv, TRACK_DIRECTORY).mkdirs();
	return rv;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter(final Environment environment) {
	final HibernateJpaVendorAdapter rv = new HibernateJpaVendorAdapter();

	rv.setDatabase(Database.H2);
	rv.setDatabasePlatform(H2Dialect.class.getName());
	rv.setGenerateDdl(environment.acceptsProfiles("dev"));
	rv.setShowSql(environment.acceptsProfiles("dev", "test"));

	return rv;
    }

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
	final JpaTransactionManager transactionManager = new JpaTransactionManager();
	transactionManager.setEntityManagerFactory(entityManagerFactory);
	return transactionManager;
    }

    @Bean
    public FactoryBean<EntityManagerFactory> entityManagerFactory(final Environment environment, final DataSource dataSource, final JpaVendorAdapter jpaVendorAdapter) {
	final Map<String, String> properties = new HashMap<>();
	properties.put("hibernate.generate_statistics", "false");
	if (environment.acceptsProfiles("dev")) {
	    properties.put("hibernate.hbm2ddl.auto", "update");
	}

	final LocalContainerEntityManagerFactoryBean rv = new LocalContainerEntityManagerFactoryBean();
	rv.setPersistenceUnitName("ac.simons.biking2.persistence.entities_resourceLocale");
	rv.setPackagesToScan("ac.simons.biking2.persistence.entities");
	rv.setJpaDialect(new HibernateJpaDialect());
	rv.setJpaVendorAdapter(jpaVendorAdapter);
	rv.setDataSource(dataSource);
	rv.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
	rv.setJpaPropertyMap(properties);
	return rv;
    }
    
    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
	return new HibernateExceptionTranslator();
    }
}
