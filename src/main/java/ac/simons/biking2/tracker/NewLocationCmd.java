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
package ac.simons.biking2.tracker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.validation.constraints.NotNull;

/**
 * @author Michael J. Simons, 2014-03-20
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewLocationCmd {

    @JsonProperty(value = "lat")
    @NotNull
    private BigDecimal latitude;

    @JsonProperty(value = "lon")
    @NotNull
    private BigDecimal longitude;

    @JsonProperty(value = "tst")
    private Long timestampSeconds;

    @JsonProperty(value = "tstMillis")
    private Long timestampMillis;

    public BigDecimal getLatitude() {
	return latitude;
    }

    public BigDecimal getLongitude() {
	return longitude;
    }

    public Calendar getCreatedAt() {
	Calendar rv = null;
	if (this.timestampSeconds != null) {
	    rv = Calendar.getInstance();
	    rv.setTimeInMillis(this.timestampSeconds * 1000);
	} else if (this.timestampMillis != null) {
	    rv = Calendar.getInstance();
	    rv.setTimeInMillis(this.timestampMillis);
	}
	return rv;
    }
}
