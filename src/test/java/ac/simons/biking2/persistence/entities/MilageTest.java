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
package ac.simons.biking2.persistence.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class MilageTest {

    @Test
    public void beanShouldWorkAsExpected() {
	Bike bike = new Bike("poef", LocalDate.now().withDayOfMonth(1));
	Milage milage = bike.addMilage(LocalDate.now().withDayOfMonth(1).plusMonths(1), 23);
	milage.prePersist();
	Milage otherMilage = bike.addMilage(LocalDate.now().withDayOfMonth(1).plusMonths(2), 50);
	otherMilage.prePersist();		
	
	// Its overwritten...
	final Bike poef = bike;
	
	// need two differend objects here...
	bike = new Bike("poef", LocalDate.now().withDayOfMonth(1));
	Milage milage2 = bike.addMilage(LocalDate.now().plusMonths(1), 23);
	
	bike = new Bike("bike2", LocalDate.now().withDayOfMonth(1));
	Milage otherMilage2 = bike.addMilage(LocalDate.now().plusMonths(1), 23);
	
	bike = new Bike("bike3", LocalDate.now().withDayOfMonth(1));
	Milage otherMilage3 = bike.addMilage(LocalDate.now().plusMonths(1), 22);
	
	Assert.assertNull(milage.getId());
	Assert.assertEquals(BigDecimal.valueOf(23d), milage.getAmount());
	Assert.assertNotNull(milage.getCreatedAt());
	Assert.assertEquals(poef, milage.getBike());
	Assert.assertEquals(milage, milage2);
	Assert.assertEquals(milage.hashCode(), milage2.hashCode());
	Assert.assertNotEquals(milage, otherMilage);
	Assert.assertNotEquals(milage, otherMilage2);
	Assert.assertNotEquals(milage, otherMilage3);
	Assert.assertNotEquals(milage, null);
	Assert.assertNotEquals(milage, "something else");
	Assert.assertTrue(milage.compareTo(otherMilage) < 0);
    }
}
