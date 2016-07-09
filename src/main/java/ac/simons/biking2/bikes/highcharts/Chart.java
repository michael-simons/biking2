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
public class Chart {

    public static class Builder<PB> {

        private final Sink<PB, Chart> sink;

        private Integer borderWidth;

        private String type;

        Builder(final Sink<PB, Chart> sink) {
            this.sink = sink;
        }

        public Builder<PB> withBorderWidth(final Integer borderWidth) {
            this.borderWidth = borderWidth;
            return this;
        }

        public Builder<PB> withType(final String type) {
            this.type = type;
            return this;
        }

        public PB build() {
            return this.sink.setObject(
                    new Chart(borderWidth, type)
            );
        }
    }

    private final Integer borderWidth;

    private final String type;

    @JsonCreator
    Chart(
            @JsonProperty("borderWidth") final Integer borderWidth,
            @JsonProperty("type") final String type
    ) {
        this.borderWidth = borderWidth;
        this.type = type;
    }

    public Integer getBorderWidth() {
        return borderWidth;
    }

    public String getType() {
        return type;
    }
}
