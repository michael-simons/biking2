/*
 * Copyright 2016-2019 michael-simons.eu.
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
package ac.simons.biking2.tracker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.TextMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Michael J. Simons
 *
 * @since 2016-07-19
 */
@ExtendWith(MockitoExtension.class)
class NewLocationMessageListenerTest {

    @Mock
    private LocationService locationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void onTextMessageShouldWork() throws IOException, JMSException {
        final TextMessage message = Mockito.mock(TextMessage.class);
        when(message.getText()).thenReturn("{\"lon\":\"5\", \"lat\":\"50\"}");

        final NewLocationMessageListener listener = new NewLocationMessageListener(objectMapper, locationService);
        listener.onMessage(message);

        final ArgumentCaptor<NewLocationCmd> locationArg = ArgumentCaptor.forClass(NewLocationCmd.class);
        verify(locationService).createAndSendNewLocation(locationArg.capture());
        final NewLocationCmd sentCmd = locationArg.getValue();
        assertThat(sentCmd.latitude(), is(new BigDecimal("50")));
        assertThat(sentCmd.longitude(), is(new BigDecimal("5")));
    }

    @Test
    void ioExceptionsShouldBeHandled() throws IOException, JMSException {
        final TextMessage message = Mockito.mock(TextMessage.class);
        when(message.getText()).thenReturn("foobar");

        final NewLocationMessageListener listener = new NewLocationMessageListener(objectMapper, locationService);
        listener.onMessage(message);

        verifyNoInteractions(locationService);
    }

    @Test
    void dataExceptionsShouldBeHandled() throws IOException, JMSException {
        final TextMessage message = Mockito.mock(TextMessage.class);
        when(message.getText()).thenReturn("{\"lon\":\"5\", \"lat\":\"50\"}");
        when(locationService.createAndSendNewLocation(any(NewLocationCmd.class))).thenThrow(new DataIntegrityViolationException("foobar"));

        final NewLocationMessageListener listener = new NewLocationMessageListener(objectMapper, locationService);
        listener.onMessage(message);

        verify(locationService).createAndSendNewLocation(any(NewLocationCmd.class));
    }

    @Test
    void onByteMessageShouldWork() throws IOException, JMSException {
        final BytesMessage message = Mockito.mock(BytesMessage.class);
        final byte[] bytes = "{\"lon\":\"5\", \"lat\":\"50\"}".getBytes();
        when(message.getBodyLength()).thenReturn((long) bytes.length);
        when(message.readBytes(any())).thenAnswer(invocation -> {
            final byte[] target = invocation.getArgument(0);
            System.arraycopy(bytes, 0, target, 0, target.length);
            return null;
        });

        final NewLocationMessageListener listener = new NewLocationMessageListener(objectMapper, locationService);
        listener.onMessage(message);

        final ArgumentCaptor<NewLocationCmd> locationArg = ArgumentCaptor.forClass(NewLocationCmd.class);
        verify(locationService).createAndSendNewLocation(locationArg.capture());
        final NewLocationCmd sentCmd = locationArg.getValue();
        assertThat(sentCmd.latitude(), is(new BigDecimal("50")));
        assertThat(sentCmd.longitude(), is(new BigDecimal("5")));
    }

    @Test
    void onOtherMessageShouldWork() throws IOException, JMSException {
        final MapMessage message = Mockito.mock(MapMessage.class);
        when(message.getJMSType()).thenReturn("foobar");

        final NewLocationMessageListener listener = new NewLocationMessageListener(objectMapper, locationService);
        listener.onMessage(message);

        verifyNoInteractions(locationService);
    }
}
