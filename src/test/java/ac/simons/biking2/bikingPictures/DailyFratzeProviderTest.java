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

package ac.simons.biking2.bikingPictures;

import java.net.URLConnection;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-05-20
 */
public class DailyFratzeProviderTest {
    @Test
    public void shouldGetRSSConnection() throws Exception {
	final DailyFratzeProvider dailyFratzeProvider = new DailyFratzeProvider("poef");
	
	URLConnection connection = dailyFratzeProvider.getRSSConnection(null);
	Assert.assertNotNull(connection);	
	Assert.assertEquals("https://dailyfratze.de/michael/tags/Theme/Radtour?format=rss&dir=d", connection.getURL().toExternalForm());
	
	String customRss = "https://dailyfratze.de/michael/tags/Theme/Radtour?format=rss&dir=a";
	connection = dailyFratzeProvider.getRSSConnection(customRss);
	Assert.assertNotNull(connection);	
	Assert.assertEquals(customRss, connection.getURL().toExternalForm());
    }    
    
    @Test
    public void shouldOpenConnectionAndAddAuthHeader() throws Exception {
	final DailyFratzeProvider dailyFratzeProvider = new DailyFratzeProvider("poef");
	
	final URLConnection connection = dailyFratzeProvider.getImageConnection(1);
	Assert.assertNotNull(connection);	
	Assert.assertEquals("https://dailyfratze.de/api/images/s/1.jpg", connection.getURL().toExternalForm());
    }    
    
    @Test
    public void shouldHandleInvalidURLsGracefully() {
	final DailyFratzeProvider dailyFratzeProvider = new DailyFratzeProvider("poef");
	URLConnection connection = dailyFratzeProvider.getRSSConnection("asd");
	Assert.assertNull(connection);	
    }
    
    @Test
    public void shouldHandleImageUrlExceptionsGracefully() {
	final DailyFratzeProvider dailyFratzeProvider = new DailyFratzeProvider("poef", "size/%s/id/%d.jpg");
	URLConnection connection = dailyFratzeProvider.getImageConnection(23);
	Assert.assertNull(connection);
    }
}
