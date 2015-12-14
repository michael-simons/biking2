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
package ac.simons.biking2.tracks.gpx;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Michael J. Simons, 2014-02-21
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Bounds {

    @XmlAttribute
    private BigDecimal minlat;

    @XmlAttribute
    private BigDecimal minlon;

    @XmlAttribute
    private BigDecimal maxlat;

    @XmlAttribute
    private BigDecimal maxlon;

    public BigDecimal getMinlat() {
	return minlat;
    }

    public BigDecimal getMinlon() {
	return minlon;
    }

    public BigDecimal getMaxlat() {
	return maxlat;
    }

    public BigDecimal getMaxlon() {
	return maxlon;
    }
}
