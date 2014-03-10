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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Michael J. Simons, 2014-03-06
 */
@ConfigurationProperties(path = "classpath:build.properties")
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
public class BuildProperties {

    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    @JsonPropertyOrder(alphabetic = true)
    public static class Versions {

	private String app;
	private String spring;
	private String springBoot;
	private String hibernate;
	private String angularjs;
	private String bootstrap;
	private String highcharts;

	public String getApp() {
	    return app;
	}

	public void setApp(String app) {
	    this.app = app;
	}

	public String getSpring() {
	    return spring;
	}

	public void setSpring(String spring) {
	    this.spring = spring;
	}

	public String getSpringBoot() {
	    return springBoot;
	}

	public void setSpringBoot(String springBoot) {
	    this.springBoot = springBoot;
	}

	public String getHibernate() {
	    return hibernate;
	}

	public void setHibernate(String hibernate) {
	    this.hibernate = hibernate;
	}

	public String getAngularjs() {
	    return angularjs;
	}

	public void setAngularjs(String angularjs) {
	    this.angularjs = angularjs;
	}

	public String getBootstrap() {
	    return bootstrap;
	}

	public void setBootstrap(String bootstrap) {
	    this.bootstrap = bootstrap;
	}

	public String getHighcharts() {
	    return highcharts;
	}

	public void setHighcharts(String highcharts) {
	    this.highcharts = highcharts;
	}

    }

    private String buildDate;
    private Versions versions;

    public String getBuildDate() {
	return buildDate;
    }

    public void setBuildDate(String buildDate) {
	this.buildDate = buildDate;
    }

    public Versions getVersions() {
	return versions;
    }

    public void setVersions(Versions versions) {
	this.versions = versions;
    }

}
