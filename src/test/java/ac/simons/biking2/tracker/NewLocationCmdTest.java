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
package ac.simons.biking2.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

/**
 * @author Michael J. Simons
 * 
 * @since 2014-05-20
 */
@JsonTest
class NewLocationCmdTest {

    @Autowired
    private JacksonTester<NewLocationCmd> json;

    /**
     * The command is primarly used through json mapping, so i choose
     * the same approach here.
     */
    @Test
    void beanShouldWorkAsExpected() throws IOException {

        var invalidNewLocationCmd = json.parseObject("{\"lon\":\"5\", \"lat\":\"50\"}");
        assertEquals(BigDecimal.valueOf(5l), invalidNewLocationCmd.longitude());
        assertEquals(BigDecimal.valueOf(50l), invalidNewLocationCmd.latitude());
        var msg = assertThrows(IllegalStateException.class,
                () -> invalidNewLocationCmd.createdAt()).getMessage();
        assertEquals("Either timestampSeconds or timestampMillis must be set.", msg);

        var newLocationCmd = json.parseObject("{\"lon\":\"5\", \"lat\":\"50\", \"tst\": \"1400577777\"}");
        final ZoneId berlin = ZoneId.of("Europe/Berlin");
        final ZoneId systemDefault = ZoneId.systemDefault();
        assertEquals(ZonedDateTime.of(2014, 5, 20, 11, 22, 57, 0, berlin)
                .withZoneSameInstant(systemDefault).toOffsetDateTime(), newLocationCmd.createdAt());

        newLocationCmd = json.parseObject("{\"lon\":\"5\", \"lat\":\"50\", \"tstMillis\": \"1400578694000\"}");
        assertEquals(ZonedDateTime.of(2014, 5, 20, 11, 38, 14, 0, berlin)
                .withZoneSameInstant(systemDefault).toOffsetDateTime(), newLocationCmd.createdAt());

        newLocationCmd = json.parseObject("{\"lon\":\"5\", \"lat\":\"50\", \"tst\": \"1400577777\", \"tstMillis\": \"1400578694000\"}");
        assertEquals(ZonedDateTime.of(2014, 5, 20, 11, 22, 57, 0, berlin)
                .withZoneSameInstant(systemDefault).toOffsetDateTime(), newLocationCmd.createdAt());
    }
}
