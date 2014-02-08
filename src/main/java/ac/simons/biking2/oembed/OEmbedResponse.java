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
package ac.simons.biking2.oembed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a copy from <a
 * href="https://github.com/michael-simons/java-oembed">java-oembed</a>
 *
 * @author Michael J. Simons, 2014-02-17
 */
@XmlRootElement(name = "oembed")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class OEmbedResponse implements Serializable {

    private static final long serialVersionUID = -887420449262020495L;

    @XmlElement(name = "type")
    private String type;
    @XmlElement(name = "version")
    private String version;
    @XmlElement(name = "title")
    private String title;
    @XmlElement(name = "author_name")
    private String authorName;
    @XmlElement(name = "author_url")
    private String authorUrl;
    @XmlElement(name = "provider_name")
    private String providerName;
    @XmlElement(name = "provider_url")
    private String providerUrl;
    @XmlElement(name = "cache_age")
    private Long cacheAge;
    @XmlElement(name = "thumbnail_url")
    private String thumbnailUrl;
    @XmlElement(name = "thumbnail_width")
    private Integer thumbnailWidth;
    @XmlElement(name = "thumbnail_height")
    private Integer thumbnailHeight;
    @XmlElement(name = "url")
    private String url;
    @XmlElement(name = "html")
    private String html;
    @XmlElement(name = "width")
    private Integer width;
    @XmlElement(name = "height")
    private Integer height;

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getVersion() {
	return version;
    }

    public void setVersion(String version) {
	this.version = version;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getAuthorName() {
	return authorName;
    }

    public void setAuthorName(String authorName) {
	this.authorName = authorName;
    }

    public String getAuthorUrl() {
	return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
	this.authorUrl = authorUrl;
    }

    public String getProviderName() {
	return providerName;
    }

    public void setProviderName(String providerName) {
	this.providerName = providerName;
    }

    public String getProviderUrl() {
	return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
	this.providerUrl = providerUrl;
    }

    public Long getCacheAge() {
	return cacheAge;
    }

    public void setCacheAge(Long cacheAge) {
	this.cacheAge = cacheAge;
    }

    public String getThumbnailUrl() {
	return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
	this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getThumbnailWidth() {
	return thumbnailWidth;
    }

    public void setThumbnailWidth(Integer thumbnailWidth) {
	this.thumbnailWidth = thumbnailWidth;
    }

    public Integer getThumbnailHeight() {
	return thumbnailHeight;
    }

    public void setThumbnailHeight(Integer thumbnailHeight) {
	this.thumbnailHeight = thumbnailHeight;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getHtml() {
	return html;
    }

    public void setHtml(String html) {
	this.html = html;
    }

    public Integer getWidth() {
	return width;
    }

    public void setWidth(Integer width) {
	this.width = width;
    }

    public Integer getHeight() {
	return height;
    }

    public void setHeight(Integer height) {
	this.height = height;
    }
}
