/*
 * Copyright 2014 Michael J. Simons.
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Michael J. Simons
 */
public class BikeTest {

    public BikeTest() {
    }

    /**
     * Test of getPeriods method, of class Bike.
     */
    @Test
    public void testGetPeriods() {

	Bike instance = new Bike();
	List<Bike.BikingPeriod> expResult = new ArrayList<>();
	List<Bike.BikingPeriod> result = instance.getPeriods();
	assertEquals(expResult, result);

	instance.getMilages().add(new Milage(instance));
	result = instance.getPeriods();
	assertEquals(expResult, result);

	instance = new Bike();
	Milage m = new Milage(instance);
	m.setRecordedAt(Date.from(LocalDate.of(2014, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
	m.setAmount(BigDecimal.valueOf(0));
	instance.getMilages().add(m);

	m = new Milage(instance);
	m.setRecordedAt(Date.from(LocalDate.of(2014, 2, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
	m.setAmount(BigDecimal.valueOf(20));
	instance.getMilages().add(m);

	m = new Milage(instance);
	m.setRecordedAt(Date.from(LocalDate.of(2014, 3, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
	m.setAmount(BigDecimal.valueOf(50));
	instance.getMilages().add(m);

	expResult = Arrays.asList(
		new Bike.BikingPeriod(LocalDate.of(2014, 1, 1), 20),
		new Bike.BikingPeriod(LocalDate.of(2014, 2, 1), 30)
	);
	result = instance.getPeriods();
	assertEquals(expResult, result);
    }

    public void testGetMilage() {
	Bike instance = new Bike();
	Milage m = new Milage(instance);
	m.setRecordedAt(Date.from(LocalDate.of(2014, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
	m.setAmount(BigDecimal.valueOf(0));
	instance.getMilages().add(m);

	m = new Milage(instance);
	m.setRecordedAt(Date.from(LocalDate.of(2014, 2, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
	m.setAmount(BigDecimal.valueOf(20));
	instance.getMilages().add(m);

	m = new Milage(instance);
	m.setRecordedAt(Date.from(LocalDate.of(2014, 3, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
	m.setAmount(BigDecimal.valueOf(50));
	instance.getMilages().add(m);

	assertEquals(new Integer(50), instance.getMilage());
    }
}
