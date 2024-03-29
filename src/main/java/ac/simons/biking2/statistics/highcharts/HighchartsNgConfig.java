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
package ac.simons.biking2.statistics.highcharts;

import ac.simons.biking2.support.Sink;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * @author Michael J. Simons
 * @since 2014-02-13
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HighchartsNgConfig {

    @SuppressWarnings({"checkstyle:hiddenfield"})
    public static class Builder {

        private final Sink<HighchartsNgConfig, HighchartsNgConfig> sink;

        private Options options;

        private Object userData;

        private final Collection<Series<?>> series = new ArrayList<>();

        Builder(final Sink<HighchartsNgConfig, HighchartsNgConfig> sink) {
            this.sink = sink;
        }

        /**
         * This is a nice example of how to create one joined stream from things
         * that have lists of other things, a reduction of several one-to-many
         * association into one stream.
         *
         * @return
         */
        public Number computeCurrentMaxYValue() {
            return series.stream()
                    .flatMap(serie -> serie.getData().stream())
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .max(Comparator.comparingDouble(Number::doubleValue)).orElse(0);
        }

        public Options.Builder<Builder> options() {
            return new Options.Builder<>(newOptions -> {
                Builder.this.options = newOptions;
                return Builder.this;
            });
        }

        public <T> Series.Builder<Builder, T> series() {
            return new Series.Builder<>(newSeries -> {
                Builder.this.series.add(newSeries);
                return Builder.this;
            });
        }

        public Builder withUserData(final Object userData) {
            this.userData = userData;
            return this;
        }

        public HighchartsNgConfig build() {
            return this.sink.setObject(new HighchartsNgConfig(options, series, userData));
        }
    }

    private final Options options;

    private final Collection<Series<?>> series;

    private final Object userData;

    @JsonCreator
    public HighchartsNgConfig(
            @JsonProperty("options") final Options options,
            @JsonProperty("series") final Collection<Series<?>> series
    ) {
        this(options, series, null);
    }

    public HighchartsNgConfig(final Options options, final Collection<Series<?>> series, final Object userData) {
        this.options = options;
        this.series = series;
        this.userData = userData;
    }

    public static Builder define() {
        return new Builder(object -> object);
    }

    public Options getOptions() {
        return options;
    }

    public Collection<Series<?>> getSeries() {
        return series;
    }

    public Object getUserData() {
        return userData;
    }
}
