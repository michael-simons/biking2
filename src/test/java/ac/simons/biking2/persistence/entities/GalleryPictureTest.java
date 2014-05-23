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
public class GalleryPictureTest {
    
    @Test
    public void beanShouldWorkAsExpected() {

	ConstructorSignatureAndPropertiesMapping mapping = new ConstructorSignatureAndPropertiesMapping();
	final List<Class<?>> signature1 = new ArrayList<>();	
	signature1.add(Calendar.class);	
	signature1.add(String.class);	
	mapping.put(signature1, Arrays.asList("takenOn", "filename"));

	final BeanLikeTester beanLikeTester = new BeanLikeTester(GalleryPicture.class, mapping);

	final Calendar now = Calendar.getInstance();
	
	final PropertiesAndValues defaultValues = new PropertiesAndValues();
	defaultValues.put("id", null);
	defaultValues.put("takenOn", null);
	defaultValues.put("filename", null);
	defaultValues.put("description", null);	
	defaultValues.put("createdAt", null);

	final PropertiesAndValues values = new PropertiesAndValues();	
	values.put("id", null);
	values.put("takenOn", now);
	values.put("filename", "filename");
	values.put("description", "description");		

	beanLikeTester.testDefaultValues(defaultValues);
	beanLikeTester.testMutatorsAndAccessors(defaultValues, values);

	final GalleryPicture bean = new GalleryPicture(now, "poef");
	bean.prePersist();
	final GalleryPicture otherBean = new GalleryPicture(now, "poef");
	Assert.assertEquals(bean, otherBean);
	Assert.assertEquals(bean.hashCode(), otherBean.hashCode());
	Assert.assertNotEquals(bean, "something else");
	Assert.assertNotEquals(bean, null);
	Assert.assertNotNull(bean.getCreatedAt());
	
	final Calendar then = Calendar.getInstance();
	then.add(Calendar.YEAR, 1);
	GalleryPicture other = new GalleryPicture(now, "something else");
	Assert.assertNotEquals(bean, other);
	other = new GalleryPicture(then, "poef");
	Assert.assertNotEquals(bean, other);		
    }
}
