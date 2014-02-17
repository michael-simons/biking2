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
package ac.simons.biking2.model.jaxb.rss;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Michael J. Simons, 2014-02-17
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Guid {

    @XmlAttribute
    private Boolean isPermaLink;
    
    @XmlValue
    private String value;

    public Boolean isIsPermaLink() {
	return isPermaLink;
    }

    public void setIsPermaLink(Boolean isPermaLink) {
	this.isPermaLink = isPermaLink;
    }
    
    public String getValue() {
	return value;
    }

    public void setValue(String value) {
	this.value = value;
    }

}
