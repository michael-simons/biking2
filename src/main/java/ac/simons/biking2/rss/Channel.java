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

import java.time.ZonedDateTime;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Michael J. Simons, 2014-02-17
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Channel {
    private String title;
    
    private String link;
    
    private String description;
    
    @XmlJavaTypeAdapter(RSSDateTimeAdapter.class)
    private ZonedDateTime pubDate;
   
    @XmlElement(name = "link", namespace = "http://www.w3.org/2005/Atom")
    private List<Link> links;

    @XmlElement(name = "item")
    private List<Item> items;
    
    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getLink() {
	return link;
    }

    public void setLink(String link) {
	this.link = link;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public ZonedDateTime getPubDate() {
	return pubDate;
    }

    public void setPubDate(ZonedDateTime pubDate) {
	this.pubDate = pubDate;
    }

    public List<Link> getLinks() {
	return links;
    }

    public void setLinks(List<Link> links) {
	this.links = links;
    }

    public List<Item> getItems() {
	return items;
    }

    public void setItems(List<Item> items) {
	this.items = items;
    }
}
