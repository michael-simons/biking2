/*
 * Copyright 2016-2017 michael-simons.eu.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * @author Michael J. Simons, 2016-07-19
 */
@RunWith(MockitoJUnitRunner.class)
public class NewLocationMessageListenerTest {

    @Mock
    private LocationService locationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void onTextMessageShouldWork() throws IOException, JMSException {
        final TextMessage message = Mockito.mock(TextMessage.class);
        when(message.getText()).thenReturn("{\"lon\":\"5\", \"lat\":\"50\"}");

        final NewLocationMessageListener listener = new NewLocationMessageListener(objectMapper, locationService);
        listener.onMessage(message);

        final ArgumentCaptor<NewLocationCmd> locationArg = ArgumentCaptor.forClass(NewLocationCmd.class);
        verify(locationService).createAndSendNewLocation(locationArg.capture());
        final NewLocationCmd sentCmd = locationArg.getValue();
        assertThat(sentCmd.getLatitude(), is(new BigDecimal("50")));
        assertThat(sentCmd.getLongitude(), is(new BigDecimal("5")));
    }

    @Test
    public void ioExceptionsShouldBeHandled() throws IOException, JMSException {
        final TextMessage message = Mockito.mock(TextMessage.class);
        when(message.getText()).thenReturn("foobar");

        final NewLocationMessageListener listener = new NewLocationMessageListener(objectMapper, locationService);
        listener.onMessage(message);

        verifyZeroInteractions(locationService);
    }
    
    @Test
    public void dataExceptionsShouldBeHandled() throws IOException, JMSException {
        final TextMessage message = Mockito.mock(TextMessage.class);
        when(message.getText()).thenReturn("{\"lon\":\"5\", \"lat\":\"50\"}");
        when(locationService.createAndSendNewLocation(any(NewLocationCmd.class))).thenThrow(new DataIntegrityViolationException("foobar"));

        final NewLocationMessageListener listener = new NewLocationMessageListener(objectMapper, locationService);
        listener.onMessage(message);

        verify(locationService).createAndSendNewLocation(any(NewLocationCmd.class));
    }

    @Test
    public void onByteMessageShouldWork() throws IOException, JMSException {
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
        assertThat(sentCmd.getLatitude(), is(new BigDecimal("50")));
        assertThat(sentCmd.getLongitude(), is(new BigDecimal("5")));
    }

    @Test
    public void onOtherMessageShouldWork() throws IOException, JMSException {
        final MapMessage message = Mockito.mock(MapMessage.class);
        when(message.getJMSType()).thenReturn("foobar");

        final NewLocationMessageListener listener = new NewLocationMessageListener(objectMapper, locationService);
        listener.onMessage(message);

        verifyZeroInteractions(locationService);
    }
}
