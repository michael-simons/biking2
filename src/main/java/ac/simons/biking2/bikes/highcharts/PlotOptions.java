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
public class PlotOptions {

    public static class Builder<PB> {

        private final Sink<PB, PlotOptions> sink;

        private Column column;

        private SeriesOptions series;

        Builder(Sink<PB, PlotOptions> sink) {
            this.sink = sink;
        }

        public Column.Builder<Builder<PB>> column() {
            return new Column.Builder<>(column -> {
                Builder.this.column = column;
                return Builder.this;
            });
        }

        public SeriesOptions.Builder<Builder<PB>> series() {
            return new SeriesOptions.Builder<>(series -> {
                Builder.this.series = series;
                return Builder.this;
            });
        }

        public PB build() {
            return this.sink.setObject(
                    new PlotOptions(column, series)
            );
        }
    }

    private final Column column;

    private final SeriesOptions series;

    @JsonCreator
    PlotOptions(
            @JsonProperty("categories") Column column,
            @JsonProperty("categories") SeriesOptions series
    ) {
        this.column = column;
        this.series = series;
    }

    public Column getColumn() {
        return column;
    }

    public SeriesOptions getSeries() {
        return series;
    }
}
