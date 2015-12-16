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

import ac.simons.biking2.config.TrackerConfig.TrackerProperties;
import ac.simons.biking2.tracker.LocationService;
import ac.simons.biking2.tracker.NewLocationCmd;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.hooks.SpringContextHook;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * @author Michael J. Simons, 2014-03-19
 */
@Configuration
@EnableConfigurationProperties(TrackerProperties.class)
@Profile({"dev", "prod"})
public class TrackerConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @ConfigurationProperties("biking2.tracker")
    public static class TrackerProperties {

	private String host;

	private int mqttPort;

	private int stompPort;

	private String username;

	private String password;
	
	private String device;
	
	private int inboundPoolSize = 1;

	private int outboundPoolSize = 2;

	private boolean useJMX = true;
	
	public String getHost() {
	    return host;
	}

	public void setHost(String host) {
	    this.host = host;
	}

	public int getMqttPort() {
	    return mqttPort;
	}

	public void setMqttPort(int mqttPort) {
	    this.mqttPort = mqttPort;
	}

	public int getStompPort() {
	    return stompPort;
	}

	public void setStompPort(int stompPort) {
	    this.stompPort = stompPort;
	}

	public String getUsername() {
	    return username;
	}

	public void setUsername(String username) {
	    this.username = username;
	}

	public String getPassword() {
	    return password;
	}

	public void setPassword(String password) {
	    this.password = password;
	}

	public String getDevice() {
	    return device;
	}

	public void setDevice(String device) {
	    this.device = device;
	}
	
	public int getInboundPoolSize() {
	    return inboundPoolSize;
	}

	public void setInboundPoolSize(int inboundPoolSize) {
	    this.inboundPoolSize = inboundPoolSize;
	}

	public int getOutboundPoolSize() {
	    return outboundPoolSize;
	}

	public void setOutboundPoolSize(int outboundPoolSize) {
	    this.outboundPoolSize = outboundPoolSize;
	}

	public boolean isUseJMX() {
	    return useJMX;
	}

	public void setUseJMX(boolean useJMX) {
	    this.useJMX = useJMX;
	}
    }

    @Autowired
    private TrackerProperties properties;

    @Bean
    public BrokerService brokerService() throws Exception {
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
	authenticationPlugin.setUsers(Arrays.asList(new AuthenticationUser(properties.getUsername(), properties.getPassword(), "")));

	rv.addShutdownHook(new SpringContextHook());
	rv.setPlugins(new BrokerPlugin[]{authenticationPlugin});
	rv.start();
	return rv;
    }

    @Bean
    public SimpleMessageListenerContainer locationMessagesContainer(
	    final LocationService locationService,
	    final ObjectMapper objectMapper,
	    final ConnectionFactory connectionFactory
    ) {
	final SimpleMessageListenerContainer rv = new SimpleMessageListenerContainer();		
	rv.setMessageListener((MessageListener) (Message message) -> {
	    String hlp = null;
	    try {
		if (message instanceof TextMessage) {
		    hlp = ((TextMessage) message).getText();
		} else if (message instanceof BytesMessage) {
		    final BytesMessage bytesMessage = (BytesMessage) message;
		    byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
		    bytesMessage.readBytes(bytes);
		    hlp = new String(bytes);
		}
	    } catch (JMSException ex) {
		Logger.getLogger(LocationService.class.getName()).log(Level.WARNING, null, ex);
	    }

	    if (hlp == null) {
		return;
	    }

	    try {
		locationService.createAndSendNewLocation(objectMapper.readValue(hlp, NewLocationCmd.class));
	    } catch (DataIntegrityViolationException | IOException ex) {
		Logger.getLogger(LocationService.class.getName()).log(Level.WARNING, null, ex);
	    }
	});
	rv.setConnectionFactory(connectionFactory);
	rv.setPubSubDomain(true);
	rv.setDestinationName(String.format("owntracks.%s.%s", properties.getUsername(), properties.getDevice()));	
	return rv;
    }

    @Bean
    public ConnectionFactory jmsConnectionFactory() {
	final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost");
	activeMQConnectionFactory.setUserName(properties.getUsername());
	activeMQConnectionFactory.setPassword(properties.getPassword());

	return new PooledConnectionFactory(activeMQConnectionFactory);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
	registry.enableStompBrokerRelay("/topic/currentLocation")
		.setRelayPort(properties.getStompPort())
		.setClientLogin(properties.getUsername())
		.setClientPasscode(properties.getPassword())
		.setSystemLogin(properties.getUsername())
		.setSystemPasscode(properties.getPassword());
	registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
	registry.addEndpoint("/api/ws").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
	registration.taskExecutor().corePoolSize(this.properties.getInboundPoolSize());
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
	registration.taskExecutor().corePoolSize(this.properties.getOutboundPoolSize());
    }
}
