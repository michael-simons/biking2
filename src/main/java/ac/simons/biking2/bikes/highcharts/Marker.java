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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Michael J. Simons, 2014-08-26
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Marker {

    public static final class Builder<PB> {

        private final Sink<PB, Marker> sink;

        private Double lineWidth;

        private String lineColor;

        private String fillColor;

        public Builder(final Sink<PB, Marker> sink) {
            this.sink = sink;
        }

        public Builder<PB> withLineWidth(final Double lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public Builder<PB> withFillColor(final String fillColor) {
            this.fillColor = fillColor;
            return this;
        }

        public Builder<PB> withLineColor(final String lineColor) {
            this.lineColor = lineColor;
            return this;
        }

        public PB build() {
            return this.sink.setObject(
                    new Marker(lineWidth, lineColor, fillColor)
            );
        }
    }

    private final Double lineWidth;

    private final String lineColor;

    private final String fillColor;

    public Marker(final Double lineWidth, final String lineColor, final String fillColor) {
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
        this.fillColor = fillColor;
    }

    public Double getLineWidth() {
        return lineWidth;
    }

    public String getLineColor() {
        return lineColor;
    }

    public String getFillColor() {
        return fillColor;
    }
}
