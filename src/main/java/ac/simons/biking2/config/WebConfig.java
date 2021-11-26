/*
 * Copyright 2014-2021 michael-simons.eu.
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

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule.Priority;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Michael J. Simons
 * @since 2014-02-15
 */
@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {

    /**
     * Allow cross origin requests for all api endpoints.
     * @param registry
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOriginPatterns("*");
    }

    /**
     * Maps all AngularJS routes to index so that they work with direct linking.
     */
    @Controller
    static class Routes {

        @GetMapping({
            "/",
            "/bikes",
            "/milages",
            "/gallery",
            "/tracks",
            "/tracks/{id:\\w+}",
            "/location",
            "/about"
        })
        public String index() {
            return "forward:/index.html";
        }
    }

    /**
     * {@code OEmbedResponse} uses XmlElement annotations to be configured for
     * JAXB as well as JSON so we need the {@link JaxbAnnotationModule} as well
     *
     * @return
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modulesToInstall(new JaxbAnnotationModule().setPriority(Priority.SECONDARY));
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> embeddedServletContainerCustomizer(
            @Value("${biking2.connector.proxyName:}") final String proxyName,
            @Value("${biking2.connector.proxyPort:80}") final int proxyPort
    ) {
        return (TomcatServletWebServerFactory server) -> {
            if (!proxyName.isEmpty()) {
                server.addConnectorCustomizers(connector -> {
                    connector.setProxyName(proxyName);
                    connector.setProxyPort(proxyPort);
                });
            }
        };
    }
}
