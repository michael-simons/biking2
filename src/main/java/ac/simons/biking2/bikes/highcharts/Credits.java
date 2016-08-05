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
package ac.simons.biking2.bikes.highcharts;

import ac.simons.biking2.support.Sink;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Michael J. Simons, 2014-02-11
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Credits {

    @SuppressWarnings({"checkstyle:hiddenfield"})
    public static final class Builder<P> {

        private final Sink<P, Credits> sink;

        private Boolean enabled = Boolean.TRUE;

        Builder(final Sink<P, Credits> sink) {
            this.sink = sink;
        }

        public Builder<P> enable() {
            this.enabled = true;
            return this;
        }

        public Builder<P> disable() {
            this.enabled = false;
            return this;
        }

        public P build() {
            return this.sink.setObject(new Credits(enabled));
        }
    }

    /** Whether to show the credits text. Defaults to true. */
    private final Boolean enabled;

    @JsonCreator
    Credits(@JsonProperty("enabled") final Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isEnabled() {
        return enabled;
    }
}
