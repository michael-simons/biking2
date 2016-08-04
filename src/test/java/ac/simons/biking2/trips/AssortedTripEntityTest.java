/*
 * Copyright 2014 michael-simons.eu.
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
package ac.simons.biking2.trips;

import java.math.BigDecimal;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class AssortedTripEntityTest {

    @Test
    public void beanShouldWorkAsExpected() {
        Calendar now = Calendar.getInstance();
        final AssortedTripEntity trip = new AssortedTripEntity(now, BigDecimal.TEN);
        Assert.assertNull(trip.getId());
        Assert.assertEquals(now, trip.getCoveredOn());
        Assert.assertEquals(BigDecimal.TEN, trip.getDistance());
        Assert.assertEquals(new AssortedTripEntity(), new AssortedTripEntity());
        Assert.assertNotEquals(new AssortedTripEntity(), null);
        Assert.assertNotEquals(new AssortedTripEntity(), "foobar");
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        Assert.assertNotEquals(trip, new AssortedTripEntity(yesterday, BigDecimal.TEN));
        Assert.assertEquals(trip.hashCode(), new AssortedTripEntity(now, BigDecimal.TEN).hashCode());
    }
}
