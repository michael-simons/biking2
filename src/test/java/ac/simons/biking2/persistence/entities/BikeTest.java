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
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

/**
 *
 * @author Michael J. Simons
 */
public class BikeTest {
    @Rule
    public final ExpectedException expectedException = none();
    
    private final Bike defaultTestBike;
    
    public BikeTest() {
	this.defaultTestBike = new Bike()
		.addMilage(LocalDate.of(2014, 1, 1), 0).getBike()
		.addMilage(LocalDate.of(2014, 2, 1), 20).getBike()
		.addMilage(LocalDate.of(2014, 3, 1), 50).getBike();	
    }
    
    @Test
    public void testGetMaxPeriod() {
	Optional<Map.Entry<LocalDate, Integer>> max = this.defaultTestBike.getMaxPeriod();		
	assertThat(max.isPresent(), is(true));
	assertThat(max.get().getKey(), is(equalTo(LocalDate.of(2014, 2, 1))));
	assertThat(max.get().getValue(), is(equalTo(30)));
	
	max = new Bike().getMaxPeriod();
	assertThat(max.isPresent(), is(false));
    }
    
    @Test
    public void testGetMinPeriod() {
	Optional<Map.Entry<LocalDate, Integer>> min = this.defaultTestBike.getMinPeriod();		
	assertThat(min.isPresent(), is(true));
	assertThat(min.get().getKey(), is(equalTo(LocalDate.of(2014, 1, 1))));
	assertThat(min.get().getValue(), is(equalTo(20)));
	
	min = new Bike().getMinPeriod();
	assertThat(min.isPresent(), is(false));
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

	instance.addMilage(LocalDate.now(), 0);
	result = instance.getPeriods();
	assertEquals(expResult, result);

	instance = this.defaultTestBike;
	
	expResult = new HashMap<>();
	expResult.put(LocalDate.of(2014, 1, 1), 20);
	expResult.put(LocalDate.of(2014, 2, 1), 30);

	result = instance.getPeriods();
	assertEquals(expResult, result);
    }

    @Test
    public void testGetMilage() {	
	assertEquals(50, defaultTestBike.getMilage());
	assertEquals(0, new Bike("test", LocalDate.now()).getMilage());
    }
    
    @Test
    public void testGetMilagesInYear() {	
	assertThat(
		defaultTestBike.getMilagesInYear(2014), 
		is(equalTo(new Integer[]{20, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}))
	);	
    }
    
    @Test
    public void testAddMilageInvalidDate() {
	this.expectedException.expect(IllegalArgumentException.class);	
	this.expectedException.expectMessage("Next valid date for milage is " + LocalDate.of(2014, 4, 1));
	this.defaultTestBike.addMilage(LocalDate.of(2015, Month.JANUARY, 1), 23);
    }    
    
    @Test
    public void testAddMilageInvalidAmount() {
	this.expectedException.expect(IllegalArgumentException.class);
	this.expectedException.expectMessage("New amount must be greater than or equal 50");
	this.defaultTestBike.addMilage(LocalDate.of(2014, Month.APRIL, 1), 23);
    }
}
