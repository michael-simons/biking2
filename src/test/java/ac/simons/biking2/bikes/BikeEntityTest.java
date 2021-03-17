/*
 * Copyright 2014-2021 michael-simons.eu.
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

import ac.simons.biking2.bikes.BikeEntity.Link;
import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Michael J. Simons
 */
class BikeEntityTest {

    private final BikeEntity defaultTestBike;

    BikeEntityTest() {
        this.defaultTestBike = new BikeEntity()
                .addMilage(LocalDate.of(2014, 1, 1), 0).getBike()
                .addMilage(LocalDate.of(2014, 2, 1), 20).getBike()
                .addMilage(LocalDate.of(2014, 3, 1), 50).getBike();
    }

    @Test
    void linkBeanShouldWorkAsExpected() {
        Link l1 = new Link();
        Link l2 = new Link("http://heise.de", "h");
        Link l3 = new Link("http://heise.de", "H");

        assertEquals(l3, l2);
        assertEquals(l3.hashCode(), l2.hashCode());
        assertNotEquals(l1, l2);
        assertNotEquals(l1.hashCode(), l2.hashCode());
        assertNotEquals(null, l2);
        assertNotEquals("asds", l2);

        assertEquals("http://heise.de", l2.getUrl());
        assertEquals("h", l2.getLabel());
        l2.setLabel("H");
        assertEquals("H", l2.getLabel());
    }

    @Test
    void beanShouldWorkAsExpected() {
        final LocalDate now = LocalDate.now();
        BikeEntity bike = new BikeEntity("poef", now.withDayOfMonth(1));

        BikeEntity same = new BikeEntity("poef", now.withDayOfMonth(1));

        BikeEntity other = new BikeEntity("other", now.withDayOfMonth(1));
        other.decommission(null);
        assertNull(other.getDecommissionedOn());
        other.decommission(now);
        assertNotNull(other.getDecommissionedOn());

        assertNull(bike.getStory());
        assertNull(bike.getId());
        assertNotNull(bike.getCreatedAt());
        assertEquals(same, bike);
        assertNotEquals(other, bike);
        assertNotEquals(null, bike);
        assertNotEquals("somethingElse", bike);
        assertNull(bike.getDecommissionedOn());
        assertEquals(now.withDayOfMonth(1), bike.getBoughtOn());

        assertEquals(now.withDayOfMonth(1), other.getBoughtOn());
        assertEquals(now, other.getDecommissionedOn());

        final BikeEntity.Link story = new BikeEntity.Link("http://planet-punk.de/2015/08/11/nie-wieder-stadtschlampe/", "Nie wieder Stadtschlampe");
        bike.setStory(story);
        assertEquals(story, bike.getStory());
    }

    @Test
    void testGetLastMilage() {
        assertEquals(50, defaultTestBike.getLastMilage());
        assertEquals(0, new BikeEntity("test", LocalDate.now()).getLastMilage());
    }

    @Test
    void testAddMilageInvalidDate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> this.defaultTestBike.addMilage(LocalDate.of(2015, Month.JANUARY, 1), 23))
                .withMessage("Next valid date for milage is " + LocalDate.of(2014, 4, 1));
        ;
    }

    @Test
    void testAddMilageInvalidAmount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> this.defaultTestBike.addMilage(LocalDate.of(2014, Month.APRIL, 1), 23))
                .withMessage("New amount must be greater than or equal 50.0");
    }
}
