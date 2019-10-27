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
package ac.simons.biking2.bikes;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 *
 * @since 2014-05-23
 */
class MilageEntityTest {

    @Test
    void beanShouldWorkAsExpected() {
        BikeEntity bike = new BikeEntity("poef", LocalDate.now().withDayOfMonth(1));
        MilageEntity milage = bike.addMilage(LocalDate.now().withDayOfMonth(1).plusMonths(1), 23);
        MilageEntity otherMilage = bike.addMilage(LocalDate.now().withDayOfMonth(1).plusMonths(2), 50);

        // Its overwritten...
        final BikeEntity poef = bike;

        // need two differend objects here...
        bike = new BikeEntity("poef", LocalDate.now().withDayOfMonth(1));
        MilageEntity milage2 = bike.addMilage(LocalDate.now().plusMonths(1), 23);

        bike = new BikeEntity("bike2", LocalDate.now().withDayOfMonth(1));
        MilageEntity otherMilage2 = bike.addMilage(LocalDate.now().plusMonths(1), 23);

        bike = new BikeEntity("bike3", LocalDate.now().withDayOfMonth(1));
        MilageEntity otherMilage3 = bike.addMilage(LocalDate.now().plusMonths(1), 22);

        Assertions.assertNull(milage.getId());
        Assertions.assertEquals(BigDecimal.valueOf(23d), milage.getAmount());
        Assertions.assertNotNull(milage.getCreatedAt());
        Assertions.assertEquals(poef, milage.getBike());
        Assertions.assertEquals(milage, milage2);
        Assertions.assertEquals(milage.hashCode(), milage2.hashCode());
        Assertions.assertNotEquals(milage, otherMilage);
        Assertions.assertNotEquals(milage, otherMilage2);
        Assertions.assertNotEquals(milage, otherMilage3);
        Assertions.assertNotEquals(milage, null);
        Assertions.assertNotEquals(milage, "something else");
        Assertions.assertTrue(milage.compareTo(otherMilage) < 0);
    }
}
