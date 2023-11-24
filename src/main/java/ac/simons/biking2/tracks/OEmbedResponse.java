/*
 * Copyright 2014-2023 michael-simons.eu.
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
package ac.simons.biking2.tracks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

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
@Getter @Setter
class OEmbedResponse implements Serializable {

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
}
