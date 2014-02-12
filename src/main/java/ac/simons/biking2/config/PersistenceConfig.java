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

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.dialect.H2Dialect;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Configuration
@EnableJpaRepositories(basePackages = "ac.simons.biking2.persistence.repositories")
public class PersistenceConfig {
    @Bean
    public JpaVendorAdapter jpaVendorAdapter(final Environment environment) {
	final HibernateJpaVendorAdapter rv = new HibernateJpaVendorAdapter();

	boolean isDev = environment.acceptsProfiles("!prod");

	rv.setDatabase(Database.H2);
	rv.setDatabasePlatform(H2Dialect.class.getName());
	rv.setGenerateDdl(isDev);
	rv.setShowSql(isDev);

	return rv;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
	return new JpaTransactionManager();
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
}