/*
 * Copyright 2014-2016 michael-simons.eu.
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
package ac.simons.biking2.bikingpictures.rss;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import static java.util.Optional.ofNullable;

/**
 * @author Michael J. Simons, 2014-02-17
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class Channel {
    private String title;

    private String link;

    private String description;

    @XmlJavaTypeAdapter(RSSDateTimeAdapter.class)
    private ZonedDateTime pubDate;

    @XmlElement(name = "link", namespace = "http://www.w3.org/2005/Atom")
    private List<Link> links;

    @XmlElement(name = "item")
    private List<Item> items;

    public String getPrevious() {
        return ofNullable(links).orElseGet(ArrayList::new).stream().filter(l -> "previous".equalsIgnoreCase(l.getRel())).findFirst().orElse(new Link()).getHref();
    }

    public String getNext() {
        return ofNullable(links).orElseGet(ArrayList::new).stream().filter(l -> "next".equalsIgnoreCase(l.getRel())).findFirst().orElse(new Link()).getHref();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(final String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public ZonedDateTime getPubDate() {
        return pubDate;
    }

    public void setPubDate(final ZonedDateTime pubDate) {
        this.pubDate = pubDate;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(final List<Link> links) {
        this.links = links;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(final List<Item> items) {
        this.items = items;
    }
}
