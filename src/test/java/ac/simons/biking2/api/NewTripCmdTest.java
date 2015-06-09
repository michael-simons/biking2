/*
 * Copyright 2015 michael-simons.eu.
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
package ac.simons.biking2.api;

import java.util.Date;
import org.junit.Test;
import org.outsideMyBox.testUtils.BeanLikeTester;
import org.outsideMyBox.testUtils.BeanLikeTester.PropertiesAndValues;

/**
 * @author Michael J. Simons, 2015-06-09
 */
public class NewTripCmdTest {

    @Test
    public void beanShouldWorkAsExpected() {
	final BeanLikeTester beanLikeTester = new BeanLikeTester(NewTripCmd.class);

	final PropertiesAndValues defaultValues = new PropertiesAndValues();
	defaultValues.put("coveredOn", null);
	defaultValues.put("distance", null);

	final PropertiesAndValues values = new PropertiesAndValues();
	values.put("coveredOn", new Date());
	values.put("distance", 2342.0);

	beanLikeTester.testMutatorsAndAccessors(defaultValues, values);
    }
}
