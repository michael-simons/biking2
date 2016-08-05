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
public final class SeriesOptions {

    @SuppressWarnings({"checkstyle:hiddenfield"})
    public static final class Builder<P> {

        private final Sink<P, SeriesOptions> sink;

        private Boolean animation;

        Builder(final Sink<P, SeriesOptions> sink) {
            this.sink = sink;
        }

        public Builder<P> enableAnimation() {
            this.animation = Boolean.TRUE;
            return this;
        }

        public Builder<P> disableAnimation() {
            this.animation = Boolean.FALSE;
            return this;
        }

        public P build() {
            return this.sink.setObject(new SeriesOptions(animation));
        }
    }

    /**
     * Enable or disable the initial animation when a series is displayed. The
     * animation can also be set as a configuration object. Please note that
     * this option only applies to the initial animation of the series itself.
     * For other animations, see chart.animation and the animation parameter
     * under the API methods.   The following properties are supported:
     */
    private final Boolean animation;

    @JsonCreator
    SeriesOptions(
            @JsonProperty("animation") final Boolean animation
    ) {
        this.animation = animation;
    }

    public Boolean isAnimation() {
        return animation;
    }
}
