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

import ac.simons.biking2.persistence.entities.Bike.Link;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.outsideMyBox.testUtils.BeanLikeTester;
import org.outsideMyBox.testUtils.BeanLikeTester.ConstructorSignatureAndPropertiesMapping;
import org.outsideMyBox.testUtils.BeanLikeTester.PropertiesAndValues;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

/**
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
    public void linkBeanShouldWorkAsExpected() {
	ConstructorSignatureAndPropertiesMapping mapping = new ConstructorSignatureAndPropertiesMapping();	
	mapping.put(Arrays.asList(String.class, String.class), Arrays.asList("url", "label"));

	final BeanLikeTester beanLikeTester = new BeanLikeTester(Bike.Link.class, mapping);
	
	final PropertiesAndValues defaultValues = new PropertiesAndValues();
	defaultValues.put("url", null);
	defaultValues.put("label", null);
	
	final PropertiesAndValues values = new PropertiesAndValues();	
	values.put("url", "http://heise.de");
	values.put("label", "foobar");

	beanLikeTester.testDefaultValues(defaultValues);
	beanLikeTester.testMutatorsAndAccessors(defaultValues, values);
	
	Link l1 = new Link();
	Link l2 = new Link("http://heise.de", "h");
	Link l3 = new Link("http://heise.de", "H");
	
	Assert.assertEquals(l2, l3);
	Assert.assertTrue(l2.hashCode() ==  l3.hashCode());
	Assert.assertNotEquals(l2, l1);
	Assert.assertFalse(l2.hashCode() ==  l1.hashCode());
	Assert.assertNotEquals(l2, null);
	Assert.assertNotEquals(l2, "asds");
    }
    
    @Test
    public void beanShouldWorkAsExpected() {
	final LocalDate now = LocalDate.now();	
	Bike bike = new Bike("poef", now.withDayOfMonth(1));
	bike.prePersist();
	
	Bike same = new Bike("poef", now.withDayOfMonth(1));
	same.prePersist();
	
	Bike other = new Bike("other", now.withDayOfMonth(1));
	other.prePersist();	
	other.decommission(null);
	Assert.assertNull(other.getDecommissionedOn());
	other.decommission(now);
	Assert.assertNotNull(other.getDecommissionedOn());
		
	Assert.assertNull(bike.getStory());
	Assert.assertNull(bike.getId());
	Assert.assertNotNull(bike.getCreatedAt());
	Assert.assertEquals(bike, same);
	Assert.assertNotEquals(bike, other);
	Assert.assertNotEquals(bike, null);
	Assert.assertNotEquals(bike, "somethingElse");
	Assert.assertNull(bike.getDecommissionedOn());
	Assert.assertEquals(GregorianCalendar.from(now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault())), bike.getBoughtOn());
	
	Assert.assertEquals(GregorianCalendar.from(now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault())), other.getBoughtOn());
	Assert.assertEquals(GregorianCalendar.from(now.atStartOfDay(ZoneId.systemDefault())), other.getDecommissionedOn());
	
	final Bike.Link story = new Bike.Link("http://planet-punk.de/2015/08/11/nie-wieder-stadtschlampe/", "Nie wieder Stadtschlampe");
	bike.setStory(story);
	Assert.assertEquals(story, bike.getStory());
    }
    
    @Test
    public void testGetMilageInYear() {
	Assert.assertThat(this.defaultTestBike.getMilageInYear(2013), is(equalTo(0)));
	Assert.assertThat(this.defaultTestBike.getMilageInYear(2014), is(equalTo(50)));
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
    public void testSummarizePeriods() {
	// Assert, that an empty list doesn't result in an error
	Map<LocalDate, Integer> summarizedPeriods = Bike.summarizePeriods(new ArrayList<>(), null);
	Assert.assertNotNull(summarizedPeriods);
	assertThat(summarizedPeriods.size(), is(0));
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
