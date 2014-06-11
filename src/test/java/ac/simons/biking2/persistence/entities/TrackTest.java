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

import ac.simons.biking2.persistence.entities.Track.Type;
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
public class TrackTest {

    @Test
    public void beanShouldWorkAsExpected() {

	ConstructorSignatureAndPropertiesMapping mapping = new ConstructorSignatureAndPropertiesMapping();
	final List<Class<?>> signature1 = new ArrayList<>();
	signature1.add(String.class);
	signature1.add(Calendar.class);	
	mapping.put(signature1, Arrays.asList("name", "coveredOn"));

	final BeanLikeTester beanLikeTester = new BeanLikeTester(Track.class, mapping);

	final Calendar now = Calendar.getInstance();
	
	final PropertiesAndValues defaultValues = new PropertiesAndValues();
	defaultValues.put("id", null);
	defaultValues.put("name", null);
	defaultValues.put("coveredOn", null);
	defaultValues.put("description", null);
	defaultValues.put("minlat", null);
	defaultValues.put("minlon", null);
	defaultValues.put("maxlat", null);
	defaultValues.put("maxlon", null);
	defaultValues.put("type", Type.biking);
	defaultValues.put("prettyId", "");

	final PropertiesAndValues values = new PropertiesAndValues();
	values.put("id", null);
	values.put("name", "name");
	values.put("coveredOn", now);
	values.put("description", "description");
	values.put("minlat", BigDecimal.ZERO);
	values.put("minlon", BigDecimal.ONE);
	values.put("maxlat", BigDecimal.ZERO.add(BigDecimal.ZERO));
	values.put("maxlon", BigDecimal.ZERO.add(BigDecimal.ZERO).add(BigDecimal.ZERO));
	values.put("type", Type.running);
	values.put("prettyId", "");

	beanLikeTester.testDefaultValues(defaultValues);
	beanLikeTester.testMutatorsAndAccessors(defaultValues, values);

	final Track t1 = new Track("t1", now);
	final Track otherT1 = new Track("t1", now);
	Assert.assertEquals(t1, otherT1);
	Assert.assertEquals(t1.hashCode(), otherT1.hashCode());
	Assert.assertNotEquals(t1, "something else");
	Assert.assertNotEquals(t1, null);
	
	final Track t2 = new Track("t2", now);
	Assert.assertNotEquals(t1, t2);
	
	final Calendar then = Calendar.getInstance();
	then.add(Calendar.YEAR, 1);
	final Track t3 = new Track("t1", then);
	Assert.assertNotEquals(t1, t3);
    }
}
