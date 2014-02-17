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
package ac.simons.biking2.model.highcharts;

import ac.simons.biking2.misc.Sink;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Michael J. Simons, 2014-02-11
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Credits {

    public static class Builder<PB> {

	private final Sink<PB, Credits> sink;

	private Boolean enabled = Boolean.TRUE;

	Builder(Sink<PB, Credits> sink) {
	    this.sink = sink;
	}

	public Builder<PB> enable() {
	    this.enabled = true;
	    return this;
	}
	
	public Builder<PB> disable() {
	    this.enabled = false;
	    return this;
	}

	public PB build() {
	    return this.sink.setObject(new Credits(enabled));
	}
    }
    
    /** Whether to show the credits text. Defaults to true. */
    private final Boolean enabled;

    @JsonCreator
    Credits(@JsonProperty("enabled") Boolean enabled) {
	this.enabled = enabled;
    }

    public Boolean isEnabled() {
	return enabled;
    }
}
