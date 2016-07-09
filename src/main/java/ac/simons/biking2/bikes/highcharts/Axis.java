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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Michael J. Simons, 2014-02-11
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Axis {

    @SuppressWarnings({"checkstyle:hiddenfield"})
    public static final class Builder<PB> {

        private final Sink<PB, Axis> sink;

        private Collection<String> categories;

        private Number min;

        private Number max;

        private Number tickInterval;

        private Boolean endOnTick;

        private Title title;

        private List<PlotLine> plotLines;

        Builder(final Sink<PB, Axis> sink) {
            this.sink = sink;
        }

        public Builder<PB> withCategories(final String... categories) {
            this.categories = Arrays.asList(categories);
            return this;
        }

        public Builder<PB> withMin(final Number min) {
            this.min = min;
            return this;
        }

        public Builder<PB> withMax(final Number max) {
            this.max = max;
            return this;
        }

        public Builder<PB> withTickInterval(final Number tickInterval) {
            this.tickInterval = tickInterval;
            return this;
        }

        public Builder<PB> enableEndOnTick() {
            this.endOnTick = true;
            return this;
        }

        public Builder<PB> disableEndOnTick() {
            this.endOnTick = false;
            return this;
        }

        public Title.Builder<Builder<PB>> title() {
            return new Title.Builder<>(t -> {
                Builder.this.title = t;
                return Builder.this;
            });
        }

        public PlotLine.Builder<Builder<PB>> withPlotLine() {
            return new PlotLine.Builder<>(plotLine -> {
                if (Builder.this.plotLines == null) {
                    Builder.this.plotLines = new ArrayList<>();
                }
                Builder.this.plotLines.add(plotLine);
                return Builder.this;
            });
        }

        public PB build() {
            return this.sink.setObject(
                    new Axis(categories, endOnTick, max, min, tickInterval, title, plotLines)
            );
        }
    }

    private final Collection<String> categories;

    private final Boolean endOnTick;

    private final Number max;

    private final Number min;

    private final Number tickInterval;

    private final Title title;

    private final Collection<PlotLine> plotLines;

    @JsonCreator
    Axis(
            @JsonProperty("categories") final Collection<String> categories,
            @JsonProperty("endOnTick") final Boolean endOnTick,
            @JsonProperty("max") final Number max,
            @JsonProperty("min") final Number min,
            @JsonProperty("tickInterval") final Number tickInterval,
            @JsonProperty("title") final Title title,
            @JsonProperty("plotLines") final Collection<PlotLine> plotLines
    ) {
        this.categories = categories;
        this.endOnTick = endOnTick;
        this.max = max;
        this.min = min;
        this.tickInterval = tickInterval;
        this.title = title;
        this.plotLines = plotLines;
    }

    public Collection<String> getCategories() {
        return categories;
    }

    public Number getMin() {
        return min;
    }

    public Number getMax() {
        return max;
    }

    public Number getTickInterval() {
        return tickInterval;
    }

    public Boolean isEndOnTick() {
        return endOnTick;
    }

    public Title getTitle() {
        return title;
    }

    public Collection<PlotLine> getPlotLines() {
        return plotLines;
    }
}
