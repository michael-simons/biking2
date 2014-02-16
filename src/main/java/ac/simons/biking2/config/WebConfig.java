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

import ac.simons.biking2.api.TracksController;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author Michael J. Simons, 2014-02-15
 */
@Configuration
public class WebConfig {

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
		return bean;
	    }

	    @Override
	    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	    }
	};
    }
}
