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

package ac.simons.biking2.bikingPictures.rss;

import ac.simons.biking2.support.BeanTester;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class ItemTest {
    @Test
    public void beanShouldWorkAsExpected() {
	final Map<String, Object> values = new HashMap<>();
	values.put("title", "title");	
	values.put("link", "link");	
	values.put("description", "description");	
	values.put("pubDate", ZonedDateTime.now());	
	values.put("guid", new Guid());	
	values.put("thumbnail", new Thumbnail());	
	values.put("content", new ArrayList<>());	
	
	values.forEach(new BeanTester(Item.class));
    }   
}