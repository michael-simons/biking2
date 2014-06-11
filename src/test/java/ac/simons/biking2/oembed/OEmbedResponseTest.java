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

package ac.simons.biking2.oembed;

import org.junit.Test;
import org.outsideMyBox.testUtils.BeanLikeTester;
import org.outsideMyBox.testUtils.BeanLikeTester.PropertiesAndValues;


/**
 * @author Michael J. Simons, 2014-05-23
 */
public class OEmbedResponseTest {
    
     @Test
    public void beanShouldWorkAsExpected() {
	final BeanLikeTester beanLikeTester = new BeanLikeTester(OEmbedResponse.class);

	final PropertiesAndValues defaultValues = new PropertiesAndValues();
	defaultValues.put("type", null);
	defaultValues.put("version", null);
	defaultValues.put("title", null);
	defaultValues.put("authorName", null);
	defaultValues.put("authorUrl", null);
	defaultValues.put("providerName", null);
	defaultValues.put("providerUrl", null);
	defaultValues.put("cacheAge", null);
	defaultValues.put("thumbnailUrl", null);
	defaultValues.put("thumbnailWidth", null);
	defaultValues.put("thumbnailHeight", null);
	defaultValues.put("url", null);
	defaultValues.put("html", null);
	defaultValues.put("width", null);
	defaultValues.put("height", null);

	beanLikeTester.testDefaultValues(defaultValues);

	final PropertiesAndValues values = new PropertiesAndValues();
	values.put("type", "type");
	values.put("version", "version");
	values.put("title", "title");
	values.put("authorName", "authorName");
	values.put("authorUrl", "authorUrl");
	values.put("providerName", "providerName");
	values.put("providerUrl", "providerUrl");
	values.put("cacheAge", 123l);
	values.put("thumbnailUrl", "thumbnailUrl");
	values.put("thumbnailWidth", 23);
	values.put("thumbnailHeight", 23);
	values.put("url", "url");
	values.put("html", "html");
	values.put("width", 42);
	values.put("height", 42);

	beanLikeTester.testMutatorsAndAccessors(defaultValues, values);
    }   
}
