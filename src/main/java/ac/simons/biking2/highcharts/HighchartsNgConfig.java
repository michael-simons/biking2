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
package ac.simons.biking2.highcharts;

import ac.simons.biking2.misc.Sink;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Michael J. Simons, 2014-02-13
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HighchartsNgConfig {

    public static class Builder {

	private final Sink<HighchartsNgConfig, HighchartsNgConfig> sink;

	private Options options;

	private final Collection<Series> series = new ArrayList<>();

	Builder(Sink<HighchartsNgConfig, HighchartsNgConfig> sink) {
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
	    return series.stream().flatMap(series -> series.getData().stream()).max((a, b) -> Double.compare(a.doubleValue(), b.doubleValue())).orElse(0);
	}

	public Options.Builder<Builder> options() {
	    return new Options.Builder<>(options -> {
		Builder.this.options = options;
		return Builder.this;
	    });
	}

	public Series.Builder<Builder> series() {
	    return new Series.Builder<>(series -> {
		Builder.this.series.add(series);
		return Builder.this;
	    });
	}

	public HighchartsNgConfig build() {
	    return this.sink.setObject(new HighchartsNgConfig(options, series));
	}
    }

    public static Builder define() {
	return new Builder(object -> object);
    }

    private final Options options;

    private final Collection<Series> series;

    @JsonCreator
    public HighchartsNgConfig(
	    @JsonProperty("options") Options options, 
	    @JsonProperty("series") Collection<Series> series)
    {
	this.options = options;
	this.series = series;
    }

    public Options getOptions() {
	return options;
    }

    public Collection<Series> getSeries() {
	return series;
    }
}
