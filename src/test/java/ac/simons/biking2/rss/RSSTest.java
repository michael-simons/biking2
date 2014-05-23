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
package ac.simons.biking2.rss;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Michael J. Simons
 */
public class RSSTest {

    @Test
    public void testJAXBMapping() throws JAXBException {
	JAXBContext context = JAXBContext.newInstance(RSS.class);

	final Unmarshaller unmarschaller = context.createUnmarshaller();
	final RSS rss = (RSS) unmarschaller.unmarshal(this.getClass().getResourceAsStream("/biking_pictures.rss"));

	assertThat(rss.getVersion(), is(equalTo("2.0")));
	assertThat(rss.getChannel().getLink(), is(equalTo("http://dailyfratze.de/michael/tags/Theme/Radtour?page=1&dir=desc")));
	assertThat(rss.getChannel().getDescription(), is(equalTo("DailyFratze.de: \"A daily picture - everyday.\".")));
	assertThat(rss.getChannel().getLinks().get(0).getHref(), is(equalTo("http://dailyfratze.de/michael/tags/Theme/Radtour?format=rss&dir=desc&page=1")));
	assertThat(rss.getChannel().getLinks().get(1).getHref(), is(equalTo("http://dailyfratze.de/michael/tags/Theme/Radtour?format=rss&dir=desc&page=2")));
	
	final Item item = rss.getChannel().getItems().get(0);
	assertThat(item.getTitle(), is(equalTo("michael - January 12, 2014")));
	assertThat(item.getLink(), is(equalTo("http://dailyfratze.de/michael/2014/1/12")));
	assertThat(item.getDescription(), is(equalTo("<img src=\"http://dailyfratze.de/fratzen/m/45644.jpg\" /><br />")));
	ZoneId berlin = ZoneId.of("Europe/Berlin");
	assertThat(item.getPubDate().withZoneSameInstant(berlin), is(equalTo(ZonedDateTime.of(2014, 1, 12, 22, 40, 25, 0, berlin))));
	assertThat(item.getGuid().getValue(), is(equalTo("http://dailyfratze.de/fratzen/m/45644.jpg")));
	assertThat(item.getThumbnail().getUrl(), is(equalTo("http://dailyfratze.de/fratzen/s/45644.jpg")));
	assertThat(item.getThumbnail().getWidth(), is(equalTo(150)));
	assertThat(item.getThumbnail().getHeight(), is(equalTo(113)));
	assertThat(item.getContent().size(), is(equalTo(2)));
	assertThat(item.getContent().get(0).getUrl(), is(equalTo("http://dailyfratze.de/fratzen/m/45644.jpg")));
	assertThat(item.getContent().get(1).getUrl(), is(equalTo("http://dailyfratze.de/fratzen/l/45644.jpg")));
	
	rss.setChannel(null);
	assertThat(rss.getChannel(), is(nullValue()));
    }    
}
