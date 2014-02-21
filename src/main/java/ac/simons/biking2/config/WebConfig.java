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

import ac.simons.biking2.api.OEmbedController;
import ac.simons.biking2.api.TracksController;
import ac.simons.biking2.oembed.OEmbedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule.Priority;
import javax.servlet.MultipartConfigElement;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

/**
 * @author Michael J. Simons, 2014-02-15
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    /**
     * Maps all AngularJS routes to index so that they work with direct linking.
     */
    @Controller
    static class Routes {

	@RequestMapping({
	    "/bikes",
	    "/milages",
	    "/tracks",
	    "/tracks/{id:\\w+}",
	    "about"
	})
	public String index() {
	    return "/index.html";
	}
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
	return new MultipartConfigElement("");
    }

    /**
     * Enable favor of format parameter over requested content type, needed for
     * {@link OEmbedController#getEmbeddableTrack(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer, javax.servlet.http.HttpServletRequest)}
     *
     * @param configurer
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
	super.configureContentNegotiation(configurer);
	configurer.favorParameter(true);
    }

    /**
     * This makes mapping of
     * {@link TracksController#getTrack(java.lang.String, java.lang.String)} and
     * the default mapping in separate methods possible.
     *
     * @return
     */
    @Bean
    public BeanPostProcessor beanPostProcessor() {
	return new BeanPostProcessor() {

	    @Override
	    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof RequestMappingHandlerMapping && "requestMappingHandlerMapping".equals(beanName)) {
		    ((RequestMappingHandlerMapping) bean).setUseRegisteredSuffixPatternMatch(true);
		}
		if (bean instanceof ThymeleafViewResolver && "thymeleafViewResolver".equals(beanName)) {
		    ((ThymeleafViewResolver) bean).setExcludedViewNames(new String[]{"/index.html"});
		}
		return bean;
	    }

	    @Override
	    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	    }
	};
    }

    /**
     * {@link OEmbedResponse} uses XmlElement annotations to be configured for
     * JAXB as well as JSON so we need the {@link JaxbAnnotationModule} as well
     *
     * @return
     */
    @Bean
    public ObjectMapper jacksonObjectMapper() {
	return new ObjectMapper().registerModules(
		new JaxbAnnotationModule().setPriority(Priority.SECONDARY)
	);
    }

    // TODO Blog
    /**
     * This is a condition based on a property. If the property is set and not
     * empty, the EmbeddedServletContainer needs a customized connector
     */
    static class NeedsCustomizedConnectorCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
	    final Environment environment = context.getEnvironment();
	    return environment.containsProperty("biking2.connector.proxyName") && !environment.getProperty("biking2.connector.proxyName", String.class).isEmpty();
	}
    }

    @Bean
    @Conditional(NeedsCustomizedConnectorCondition.class)
    public EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer(
	    final @Value("${biking2.connector.proxyName}") String proxyName,
	    final @Value("${biking2.connector.proxyPort:80}") int proxyPort
    ) {
	return (ConfigurableEmbeddedServletContainerFactory factory) -> {
	    if (factory instanceof TomcatEmbeddedServletContainerFactory) {
		final TomcatEmbeddedServletContainerFactory containerFactory = (TomcatEmbeddedServletContainerFactory) factory;
		containerFactory.addConnectorCustomizers(connector -> {
		    connector.setProxyName(proxyName);
		    connector.setProxyPort(proxyPort);
		});
	    }
	};
    }
}
