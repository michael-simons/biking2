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

import ac.simons.biking2.config.LocationServiceConfig.LocationServiceProperties;
import ac.simons.biking2.persistence.entities.Location;
import ac.simons.biking2.persistence.repositories.LocationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * @author Michael J. Simons, 2014-03-19
 */
@Configuration
@EnableConfigurationProperties(LocationServiceProperties.class)
@EnableWebSocketMessageBroker
public class LocationServiceConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @ConfigurationProperties("biking2.locationService")
    public static class LocationServiceProperties {

	private String host;

	private int mqttPort;
	
	private int stompPort;

	private String username;

	private String password;

	private String topic;

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

	public String getTopic() {
	    return topic;
	}

	public void setTopic(String topic) {
	    this.topic = topic;
	}
    }

    @Autowired
    private LocationServiceProperties properties;
    
    @Bean
    public BrokerService brokerService() throws Exception {
	final BrokerService rv = BrokerFactory.createBroker(		
		String.format("broker:("
			+ "vm://localhost,"
			+ "stomp://localhost:%d,"
			+ "mqtt+nio://%s:%d"
			+ ")?persistent=false&useJmx=true&useShutdownHook=true", 
			properties.getStompPort(),
			properties.getHost(), 
			properties.getMqttPort()
		)
	);

	final SimpleAuthenticationPlugin authenticationPlugin = new SimpleAuthenticationPlugin();
	authenticationPlugin.setAnonymousAccessAllowed(false);
	authenticationPlugin.setUsers(Arrays.asList(new AuthenticationUser(properties.getUsername(), properties.getPassword(), "")));	
		
	rv.setPlugins(new BrokerPlugin[]{authenticationPlugin});
	rv.start();
	return rv;
    }

    @Bean
    public ConnectionFactory jmsConnectionFactory() {
	PooledConnectionFactory pool = new PooledConnectionFactory();
	final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost");
	activeMQConnectionFactory.setUserName(properties.getUsername());
	activeMQConnectionFactory.setPassword(properties.getPassword());
	pool.setConnectionFactory(activeMQConnectionFactory);
	return pool;
    }

    @Bean
    public SimpleMessageListenerContainer locationMessagesContainer(
	    final LocationRepository locationRepository, 
	    final ObjectMapper objectMapper,
	    final ConnectionFactory connectionFactory
    ) {
	final MessageListenerAdapter messageListener = new MessageListenerAdapter(locationRepository);
	messageListener.setDefaultListenerMethod("save");
	messageListener.setDefaultResponseTopicName("currentLocation");
	messageListener.setMessageConverter(new MessageConverter() {
	    @Override
	    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
		try {
		    return session.createTextMessage(objectMapper.writeValueAsString(object));
		} catch (JsonProcessingException ex) {
		    throw new MessageConversionException("Could not convert to message.", ex);
		} 
	    }

	    @Override
	    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
		String hlp = null;
		if (message instanceof TextMessage) {
		    hlp = ((TextMessage) message).getText();
		} else if (message instanceof BytesMessage) {
		    final BytesMessage bytesMessage = (BytesMessage) message;
		    byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
		    bytesMessage.readBytes(bytes);
		    hlp = new String(bytes);
		} else {
		    throw new UnsupportedOperationException(String.format("Unsupported message type: %s.", message.getClass()));
		}

		try {
		    return objectMapper.readValue(hlp, Location.class);
		} catch (IOException ex) {
		    throw new MessageConversionException("Could not convert to event.", ex);
		}
	    }
	});
	
	
	final SimpleMessageListenerContainer rv = new SimpleMessageListenerContainer();
	rv.setMessageListener(messageListener);
	rv.setConnectionFactory(connectionFactory);
	rv.setDestinationName("locations");
	rv.setPubSubDomain(true);

	return rv;
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
}
