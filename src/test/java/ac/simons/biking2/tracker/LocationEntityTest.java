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
package ac.simons.biking2.tracker;

import ac.simons.biking2.support.BeanTester;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class LocationEntityTest {

    @Test
    public void beanShouldWorkAsExpected() {
        final Calendar now = Calendar.getInstance();

        final Map<String, Object> values = new HashMap<>();
        values.put("description", "description");

        values.forEach(new BeanTester(LocationEntity.class));

        final LocationEntity l1 = new LocationEntity(BigDecimal.ONE, BigDecimal.TEN, now);
        final LocationEntity otherL1 = new LocationEntity(BigDecimal.ONE, BigDecimal.TEN, now);
        Assert.assertEquals(l1, otherL1);
        Assert.assertEquals(l1.hashCode(), otherL1.hashCode());
        Assert.assertNotEquals(l1, "something else");
        Assert.assertNotEquals(l1, null);
        Assert.assertEquals(BigDecimal.ONE, l1.getLatitude());
        Assert.assertEquals(BigDecimal.TEN, l1.getLongitude());
        Assert.assertEquals(now, l1.getCreatedAt());
        Assert.assertNull(l1.getId());

        final Calendar then = Calendar.getInstance();
        then.add(Calendar.YEAR, 1);
        LocationEntity other = new LocationEntity(BigDecimal.ZERO, BigDecimal.TEN, now);
        Assert.assertNotEquals(l1, other);
        other = new LocationEntity(BigDecimal.ONE, BigDecimal.ZERO, now);
        Assert.assertNotEquals(l1, other);
        other = new LocationEntity(BigDecimal.ONE, BigDecimal.TEN, then);
        Assert.assertNotEquals(l1, other);
    }
}
