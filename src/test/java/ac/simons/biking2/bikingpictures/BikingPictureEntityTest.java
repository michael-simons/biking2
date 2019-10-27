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
package ac.simons.biking2.bikingpictures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons, 2014-05-23
 */
class BikingPictureEntityTest {

    @Test
    void beanShouldWorkAsExpected() {
        // Test default constructor
        BikingPictureEntity bikingPicture;

        bikingPicture = new BikingPictureEntity();
        assertEquals(bikingPicture, new BikingPictureEntity());
        assertNull(bikingPicture.getExternalId());
        assertNull(bikingPicture.getId());
        assertNull(bikingPicture.getLink());
        assertNull(bikingPicture.getPubDate());

        final ZonedDateTime now = ZonedDateTime.now();
        bikingPicture = new BikingPictureEntity("http://dailyfratze.de/fratzen/m/45644.jpg", now, "http://dailyfratze.de/michael/2014/1/12");

        assertNotEquals(bikingPicture, new BikingPictureEntity());
        assertNotEquals(bikingPicture, null);
        assertNotEquals(bikingPicture, "not equals");
        assertEquals(Integer.valueOf(45644), bikingPicture.getExternalId());
        assertNull(bikingPicture.getId());
        assertEquals("http://dailyfratze.de/michael/2014/1/12", bikingPicture.getLink());
        assertEquals(now.toOffsetDateTime(), bikingPicture.getPubDate());

        final BikingPictureEntity bikingPicture2 = new BikingPictureEntity("http://dailyfratze.de/fratzen/m/45644.jpg", now, "http://dailyfratze.de/michael/2014/1/12");
        assertEquals(bikingPicture, bikingPicture2);
        assertEquals(bikingPicture.hashCode(), bikingPicture2.hashCode());
    }

    @Test
    void shouldHandleInvalidGuidsGracefully() {

        assertThrows(InvalidGUIDException.class, () -> {
            final ZonedDateTime now = ZonedDateTime.now();
            new BikingPictureEntity("http://www.heise.de", now, "http://www.heise.de");
        });
    }
}
