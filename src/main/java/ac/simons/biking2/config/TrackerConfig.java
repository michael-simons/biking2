/*
 * Copyright 2014-2019 michael-simons.eu.
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

import ac.simons.biking2.config.TrackerConfig.TrackerProperties;
import ac.simons.biking2.tracker.NewLocationMessageListener;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.jms.ConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.hooks.SpringContextHook;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author Michael J. Simons, 2014-03-19
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(TrackerProperties.class)
@Profile({"default", "prod"})
public class TrackerConfig implements WebSocketMessageBrokerConfigurer {

    @ConfigurationProperties("biking2.tracker")
    public static class TrackerProperties {

        private String host;

        private int mqttPort;

        private int stompPort;

        private String device;

        private int inboundPoolSize = 1;

        private int outboundPoolSize = 2;

        private boolean useJMX = true;

        public String getHost() {
            return host;
        }

        public void setHost(final String host) {
            this.host = host;
        }

        public int getMqttPort() {
            return mqttPort;
        }

        public void setMqttPort(final int mqttPort) {
            this.mqttPort = mqttPort;
        }

        public int getStompPort() {
            return stompPort;
        }

        public void setStompPort(final int stompPort) {
            this.stompPort = stompPort;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(final String device) {
            this.device = device;
        }

        public int getInboundPoolSize() {
            return inboundPoolSize;
        }

        public void setInboundPoolSize(final int inboundPoolSize) {
            this.inboundPoolSize = inboundPoolSize;
        }

        public int getOutboundPoolSize() {
            return outboundPoolSize;
        }

        public void setOutboundPoolSize(final int outboundPoolSize) {
            this.outboundPoolSize = outboundPoolSize;
        }

        public boolean isUseJMX() {
            return useJMX;
        }

        public void setUseJMX(final boolean useJMX) {
            this.useJMX = useJMX;
        }
    }

    private final TrackerProperties properties;

    private final SecurityProperties.User user;

    public TrackerConfig(final TrackerProperties properties, final SecurityProperties securityProperties) {
        this.properties = properties;
        this.user = securityProperties.getUser();
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService taskScheduler(@Value("${biking2.scheduled-thread-pool-size:10}") final int scheduledThreadPoolSize) {
        return Executors.newScheduledThreadPool(scheduledThreadPoolSize);
    }

    @Bean
    public SpringContextHook springContextHook() {
        return new SpringContextHook();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService brokerService(final SpringContextHook springContextHook) throws Exception {
        final BrokerService rv = BrokerFactory.createBroker(
                String.format("broker:("
                        + "vm://localhost,"
                        + "stomp://localhost:%d,"
                        + "mqtt+nio://%s:%d"
                        + ")?persistent=false&useJmx=%s&useShutdownHook=true",
                        properties.getStompPort(),
                        properties.getHost(),
                        properties.getMqttPort(),
                        properties.isUseJMX()
                )
        );

        final SimpleAuthenticationPlugin authenticationPlugin = new SimpleAuthenticationPlugin();
        authenticationPlugin.setAnonymousAccessAllowed(false);
        authenticationPlugin.setUsers(Arrays.asList(new AuthenticationUser(user.getName(), user.getPassword(), "")));

        rv.addShutdownHook(springContextHook);
        rv.setPlugins(new BrokerPlugin[]{authenticationPlugin});
        return rv;
    }

    @Bean
    public SimpleMessageListenerContainer locationMessagesContainer(
            final NewLocationMessageListener newLocationMessageListener,
            final ConnectionFactory connectionFactory
    ) {
        final SimpleMessageListenerContainer rv = new SimpleMessageListenerContainer();
        rv.setMessageListener(newLocationMessageListener);
        rv.setConnectionFactory(connectionFactory);
        rv.setPubSubDomain(true);
        rv.setDestinationName(String.format("owntracks.%s.%s", user.getName(), properties.getDevice()));
        return rv;
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/topic/currentLocation")
                .setRelayPort(properties.getStompPort())
                .setClientLogin(user.getName())
                .setClientPasscode(user.getPassword())
                .setSystemLogin(user.getName())
                .setSystemPasscode(user.getPassword());
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(final ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(this.properties.getInboundPoolSize());
    }

    @Override
    public void configureClientOutboundChannel(final ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(this.properties.getOutboundPoolSize());
    }
}
