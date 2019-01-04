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

import ac.simons.biking2.tracks.TrackEntity.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class TrackEntityTest {

    @Test
    public void beanShouldWorkAsExpected() {
        final LocalDate now = LocalDate.now();

        final TrackEntity bean = new TrackEntity("name", now);
        Assert.assertNull(bean.getId());
        Assert.assertEquals("name", bean.getName());
        Assert.assertEquals(now, bean.getCoveredOn());
        bean.setDescription("description");
        Assert.assertEquals("description", bean.getDescription());
        bean.setMinlat( BigDecimal.ZERO);
        Assert.assertEquals( BigDecimal.ZERO, bean.getMinlat());
        bean.setMinlon(BigDecimal.ONE);
        Assert.assertEquals(BigDecimal.ONE, bean.getMinlon());
        bean.setMaxlat(BigDecimal.ZERO.add(BigDecimal.ZERO));
        Assert.assertEquals(BigDecimal.ZERO.add(BigDecimal.ZERO), bean.getMaxlat());
        bean.setMaxlon(BigDecimal.ZERO.add(BigDecimal.ZERO).add(BigDecimal.ZERO));
        Assert.assertEquals(BigDecimal.ZERO.add(BigDecimal.ZERO).add(BigDecimal.ZERO), bean.getMaxlon());
        bean.setType(Type.running);
        Assert.assertEquals(Type.running, bean.getType());
        Assert.assertEquals("", bean.getPrettyId());

        final TrackEntity t1 = new TrackEntity("t1", now);
        final TrackEntity otherT1 = new TrackEntity("t1", now);
        Assert.assertEquals(t1, otherT1);
        Assert.assertEquals(t1.hashCode(), otherT1.hashCode());
        Assert.assertNotEquals(t1, "something else");
        Assert.assertNotEquals(t1, null);

        final TrackEntity t2 = new TrackEntity("t2", now);
        Assert.assertNotEquals(t1, t2);

        final LocalDate then = LocalDate.now().plusYears(1);
        final TrackEntity t3 = new TrackEntity("t1", then);
        Assert.assertNotEquals(t1, t3);
    }
}
