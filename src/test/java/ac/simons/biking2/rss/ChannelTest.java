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
package ac.simons.biking2.rss;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;
import org.outsideMyBox.testUtils.BeanLikeTester;
import org.outsideMyBox.testUtils.BeanLikeTester.PropertiesAndValues;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class ChannelTest {

    @Test
    public void beanShouldWorkAsExpected() {
	final BeanLikeTester beanLikeTester = new BeanLikeTester(Channel.class);

	final PropertiesAndValues defaultValues = new PropertiesAndValues();
	defaultValues.put("title", null);
	defaultValues.put("link", null);
	defaultValues.put("description", null);
	defaultValues.put("pubDate", null);
	defaultValues.put("links", null);
	defaultValues.put("items", null);
        defaultValues.put("previous", null);
	defaultValues.put("next", null);

	beanLikeTester.testDefaultValues(defaultValues);

	final PropertiesAndValues values = new PropertiesAndValues();
	values.put("title", "title");
	values.put("link", "link");
	values.put("description", "description");
	values.put("pubDate", ZonedDateTime.now());
	values.put("links", new ArrayList<>());
	values.put("items", new ArrayList<>());

	beanLikeTester.testMutatorsAndAccessors(defaultValues, values);
    }

    @Test
    public void previousAndNextShouldWorkAsExpected() {
	final Channel channel = new Channel();
	channel.setLinks(new ArrayList<>());
	
	Link link;
	
	link = new Link();
	link.setRel("previous");
	link.setHref("p1");
	channel.getLinks().add(link);
	
	link = new Link();
	link.setRel("next");
	link.setHref("n1");
	channel.getLinks().add(link);
	
	link = new Link();
	link.setRel("previous");
	link.setHref("p2");
	channel.getLinks().add(link);
	
	link = new Link();
	link.setRel("next");
	link.setHref("n2");
	channel.getLinks().add(link);
	
	Assert.assertEquals("p1", channel.getPrevious());
	Assert.assertEquals("n1", channel.getNext());	
    }
}
