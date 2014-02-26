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
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.filter.CharacterEncodingFilter;
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
	    "/gallery",
	    "/tracks",
	    "/tracks/{id:\\w+}",
	    "about"
	})
	public String index() {
	    return "/index.html";
	}
    }
    
    @Bean
    public ServletContextInitializer servletContextInitializer() {
	return (ServletContext servletContext) -> {
	    final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
	    characterEncodingFilter.setEncoding("UTF-8");
	    characterEncodingFilter.setForceEncoding(false);
	    
	    servletContext.addFilter("characterEncodingFilter", characterEncodingFilter).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
	};
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
	return new MultipartConfigElement("", 5 * 1024 * 1024, 5 * 1024 * 1024, 1024 * 1024);	
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

    @Bean
    @ConditionalOnExpression(value = "environment['biking2.connector.proxyName'] != null && !environment['biking2.connector.proxyName'].isEmpty()")
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
