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
package ac.simons.biking2.tracks.gpx;

import ac.simons.biking2.tracks.gpx.GPX;
import ac.simons.biking2.tracks.gpx.Bounds;
import org.junit.Test;
import org.outsideMyBox.testUtils.BeanLikeTester;
import org.outsideMyBox.testUtils.BeanLikeTester.PropertiesAndValues;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class GPXTest {

    @Test
    public void beanShouldWorkAsExpected() {
	final BeanLikeTester beanLikeTester = new BeanLikeTester(GPX.class);

	final PropertiesAndValues defaultValues = new PropertiesAndValues();
	defaultValues.put("bounds", null);

	beanLikeTester.testDefaultValues(defaultValues);

	final PropertiesAndValues values = new PropertiesAndValues();
	values.put("bounds", new Bounds());

	beanLikeTester.testMutatorsAndAccessors(defaultValues, values);
    }
}
