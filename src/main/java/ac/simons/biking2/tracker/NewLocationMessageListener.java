/*
 * Copyright 2016-2023 michael-simons.eu.
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
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Takes care of incoming messages that may contain new locations.
 *
 * @author Michael J. Simons
 * @since 2016-07-19
 */
@Component
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class NewLocationMessageListener implements MessageListener {

    private final ObjectMapper objectMapper;

    private final LocationService locationService;

    @Override
    public void onMessage(final Message message) {
        try {
            final String hlp;
            if (message instanceof TextMessage m) {
                hlp = m.getText();
            } else if (message instanceof BytesMessage m) {
                byte[] bytes = new byte[(int) m.getBodyLength()];
                m.readBytes(bytes);
                hlp = new String(bytes);
            } else {
                throw new JMSException("Unsupported message type: " + message.getJMSType());
            }

            locationService.createAndSendNewLocation(objectMapper.readValue(hlp, NewLocationCmd.class));
        } catch (JMSException ex) {
            log.warn("Could not handle location message...", ex);
        } catch (DataIntegrityViolationException | IOException ex) {
            log.warn("Could not store new location...", ex);
        }
    }
}
