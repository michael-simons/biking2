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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.outsideMyBox.testUtils.BeanLikeTester;
import org.outsideMyBox.testUtils.BeanLikeTester.ConstructorSignatureAndPropertiesMapping;
import org.outsideMyBox.testUtils.BeanLikeTester.PropertiesAndValues;


/**
 * @author Michael J. Simons, 2014-05-23
 */
public class LocationTest {
    
    @Test
    public void beanShouldWorkAsExpected() {

	ConstructorSignatureAndPropertiesMapping mapping = new ConstructorSignatureAndPropertiesMapping();
	final List<Class<?>> signature1 = new ArrayList<>();
	signature1.add(BigDecimal.class);	
	signature1.add(BigDecimal.class);	
	signature1.add(Calendar.class);	
	mapping.put(signature1, Arrays.asList("latitude", "longitude", "createdAt"));

	final BeanLikeTester beanLikeTester = new BeanLikeTester(Location.class, mapping);

	final Calendar now = Calendar.getInstance();
	
	final PropertiesAndValues defaultValues = new PropertiesAndValues();
	defaultValues.put("id", null);
	defaultValues.put("latitude", null);
	defaultValues.put("longitude", null);
	defaultValues.put("createdAt", null);	
	defaultValues.put("description", null);

	final PropertiesAndValues values = new PropertiesAndValues();	
	values.put("id", null);
	values.put("latitude", BigDecimal.ONE);
	values.put("longitude", BigDecimal.TEN);
	values.put("createdAt", now);
	values.put("description", "description");

	beanLikeTester.testDefaultValues(defaultValues);
	beanLikeTester.testMutatorsAndAccessors(defaultValues, values);

	final Location l1 = new Location(BigDecimal.ONE, BigDecimal.TEN, now);
	final Location otherL1 = new Location(BigDecimal.ONE, BigDecimal.TEN, now);
	Assert.assertEquals(l1, otherL1);
	Assert.assertEquals(l1.hashCode(), otherL1.hashCode());
	Assert.assertNotEquals(l1, "something else");
	Assert.assertNotEquals(l1, null);
	
	final Calendar then = Calendar.getInstance();
	then.add(Calendar.YEAR, 1);
	Location other = new Location(BigDecimal.ZERO, BigDecimal.TEN, now);
	Assert.assertNotEquals(l1, other);
	other = new Location(BigDecimal.ONE, BigDecimal.ZERO, now);
	Assert.assertNotEquals(l1, other);
	other = new Location(BigDecimal.ONE, BigDecimal.TEN, then);
	Assert.assertNotEquals(l1, other);
    }
}
