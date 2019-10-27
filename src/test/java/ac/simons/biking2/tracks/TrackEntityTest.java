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
package ac.simons.biking2.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ac.simons.biking2.tracks.TrackEntity.Type;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 * 
 * @since 2014-05-23
 */
class TrackEntityTest {

    @Test
    public void beanShouldWorkAsExpected() {
        final LocalDate now = LocalDate.now();

        final TrackEntity bean = new TrackEntity("name", now);
        assertNull(bean.getId());
        assertEquals("name", bean.getName());
        assertEquals(now, bean.getCoveredOn());
        bean.setDescription("description");
        assertEquals("description", bean.getDescription());
        bean.setMinlat( BigDecimal.ZERO);
        assertEquals( BigDecimal.ZERO, bean.getMinlat());
        bean.setMinlon(BigDecimal.ONE);
        assertEquals(BigDecimal.ONE, bean.getMinlon());
        bean.setMaxlat(BigDecimal.ZERO.add(BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO.add(BigDecimal.ZERO), bean.getMaxlat());
        bean.setMaxlon(BigDecimal.ZERO.add(BigDecimal.ZERO).add(BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO.add(BigDecimal.ZERO).add(BigDecimal.ZERO), bean.getMaxlon());
        bean.setType(Type.running);
        assertEquals(Type.running, bean.getType());
        assertEquals("", bean.getPrettyId());

        final TrackEntity t1 = new TrackEntity("t1", now);
        final TrackEntity otherT1 = new TrackEntity("t1", now);
        assertEquals(t1, otherT1);
        assertEquals(t1.hashCode(), otherT1.hashCode());
        assertNotEquals(t1, "something else");
        assertNotEquals(t1, null);

        final TrackEntity t2 = new TrackEntity("t2", now);
        assertNotEquals(t1, t2);

        final LocalDate then = LocalDate.now().plusYears(1);
        final TrackEntity t3 = new TrackEntity("t1", then);
        assertNotEquals(t1, t3);
    }
}
