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

import java.time.ZonedDateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.rules.ExpectedException.none;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class BikingPictureEntityTest {
    @Rule
    public final ExpectedException expectedException = none();

    @Test
    public void beanShouldWorkAsExpected() {
        // Test default constructor
        BikingPictureEntity bikingPicture;

        bikingPicture = new BikingPictureEntity();
        Assert.assertEquals(bikingPicture, new BikingPictureEntity());
        Assert.assertNull(bikingPicture.getExternalId());
        Assert.assertNull(bikingPicture.getId());
        Assert.assertNull(bikingPicture.getLink());
        Assert.assertNull(bikingPicture.getPubDate());

        final ZonedDateTime now = ZonedDateTime.now();
        bikingPicture = new BikingPictureEntity("http://dailyfratze.de/fratzen/m/45644.jpg", now, "http://dailyfratze.de/michael/2014/1/12");

        Assert.assertNotEquals(bikingPicture, new BikingPictureEntity());
        Assert.assertNotEquals(bikingPicture, null);
        Assert.assertNotEquals(bikingPicture, "not equals");
        Assert.assertEquals(Integer.valueOf(45644), bikingPicture.getExternalId());
        Assert.assertNull(bikingPicture.getId());
        Assert.assertEquals("http://dailyfratze.de/michael/2014/1/12", bikingPicture.getLink());
        Assert.assertEquals(now.toOffsetDateTime(), bikingPicture.getPubDate());

        final BikingPictureEntity bikingPicture2 = new BikingPictureEntity("http://dailyfratze.de/fratzen/m/45644.jpg", now, "http://dailyfratze.de/michael/2014/1/12");
        Assert.assertEquals(bikingPicture, bikingPicture2);
        Assert.assertEquals(bikingPicture.hashCode(), bikingPicture2.hashCode());
    }

    @Test
    public void shouldHandleInvalidGuidsGracefully() {
        this.expectedException.expect(InvalidGUIDException.class);

        final ZonedDateTime now = ZonedDateTime.now();
        new BikingPictureEntity("http://www.heise.de", now, "http://www.heise.de");
    }
}
