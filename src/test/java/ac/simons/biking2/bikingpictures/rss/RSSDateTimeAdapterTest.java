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

package ac.simons.biking2.bikingpictures.rss;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @author Michael J. Simons
 *
 * @since 2014-05-22
 */
class RSSDateTimeAdapterTest {

    @Test
    void shouldMarshallStuff() {
        final RSSDateTimeAdapter rSSDateTimeAdapter = new RSSDateTimeAdapter();
        Assertions.assertEquals("Thu, 22 May 2014 12:59:23 GMT", rSSDateTimeAdapter.marshal(ZonedDateTime.of(2014, 5, 22, 12, 59, 23, 0, ZoneId.of("GMT"))));
    }

    @Test
    void shouldUnmarshallStuff() {
        final RSSDateTimeAdapter rSSDateTimeAdapter = new RSSDateTimeAdapter();
        Assertions.assertEquals(ZonedDateTime.of(2014, 5, 22, 12, 59, 23, 0, ZoneId.of("GMT")), rSSDateTimeAdapter.unmarshal("Thu, 22 May 2014 12:59:23 GMT"));
    }
}
