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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Michael J. Simons
 */
public class BikeTest {
    private final Bike defaultTestBike;
    
    public BikeTest() {
	this.defaultTestBike = new Bike();

	Milage m = new Milage(this.defaultTestBike, LocalDate.of(2014, 1, 1), 0);
	this.defaultTestBike.getMilages().add(m);

	m = new Milage(this.defaultTestBike, LocalDate.of(2014, 2, 1), 20);
	this.defaultTestBike.getMilages().add(m);

	m = new Milage(this.defaultTestBike, LocalDate.of(2014, 3, 1), 50);
	this.defaultTestBike.getMilages().add(m);	
    }

    /**
     * Test of getPeriods method, of class Bike.
     */
    @Test
    public void testGetPeriods() {

	Bike instance = new Bike();
	Map<LocalDate, Integer> expResult = new HashMap<>();
	Map<LocalDate, Integer> result = instance.getPeriods();
	assertEquals(expResult, result);

	instance.getMilages().add(new Milage(instance, LocalDate.now(), 0));
	result = instance.getPeriods();
	assertEquals(expResult, result);

	instance = new Bike();
	Milage m = new Milage(instance, LocalDate.of(2014, 1, 1), 0);
	instance.getMilages().add(m);

	m = new Milage(instance, LocalDate.of(2014, 2, 1), 20);
	instance.getMilages().add(m);

	m = new Milage(instance, LocalDate.of(2014, 3, 1), 50);
	instance.getMilages().add(m);

	expResult = new HashMap<>();
	expResult.put(LocalDate.of(2014, 1, 1), 20);
	expResult.put(LocalDate.of(2014, 2, 1), 30);

	result = instance.getPeriods();
	assertEquals(expResult, result);
    }

    @Test
    public void testGetMilage() {	
	assertEquals(new Integer(50), defaultTestBike.getMilage());
    }
    
    @Test
    public void testGetMilagesInYear() {	
	assertThat(
		defaultTestBike.getMilagesInYear(2014), 
		is(equalTo(new Integer[]{20, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}))
	);	
    }
}
