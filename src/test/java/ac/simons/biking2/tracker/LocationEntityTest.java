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

import ac.simons.biking2.support.BeanTester;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 * 
 * @since 2014-05-23
 */
class LocationEntityTest {

    @Test
    void beanShouldWorkAsExpected() {
        final OffsetDateTime now = OffsetDateTime.now();

        final Map<String, Object> values = Map.of("description", "description");

        values.forEach(new BeanTester(LocationEntity.class));

        final LocationEntity l1 = new LocationEntity(BigDecimal.ONE, BigDecimal.TEN, now);
        final LocationEntity otherL1 = new LocationEntity(BigDecimal.ONE, BigDecimal.TEN, now);
        Assertions.assertEquals(l1, otherL1);
        Assertions.assertEquals(l1.hashCode(), otherL1.hashCode());
        Assertions.assertNotEquals(l1, "something else");
        Assertions.assertNotEquals(l1, null);
        Assertions.assertEquals(BigDecimal.ONE, l1.getLatitude());
        Assertions.assertEquals(BigDecimal.TEN, l1.getLongitude());
        Assertions.assertEquals(now, l1.getCreatedAt());
        Assertions.assertNull(l1.getId());

        final OffsetDateTime then = OffsetDateTime.now().plusYears(1);
        LocationEntity other = new LocationEntity(BigDecimal.ZERO, BigDecimal.TEN, now);
        Assertions.assertNotEquals(l1, other);
        other = new LocationEntity(BigDecimal.ONE, BigDecimal.ZERO, now);
        Assertions.assertNotEquals(l1, other);
        other = new LocationEntity(BigDecimal.ONE, BigDecimal.TEN, then);
        Assertions.assertNotEquals(l1, other);
    }
}
