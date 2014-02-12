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
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Michael J. Simons, 2014-02-11
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HighchartDefinition {
    public static class Builder {

	private final Sink<HighchartDefinition, HighchartDefinition> sink;

	private Chart chart;

	private Credits credits;

	private PlotOptions plotOptions;

	private Collection<Series> series = new ArrayList<>();
	
	private Title title;

	private Tooltip tooltip;

	private Axis xAxis;

	private Axis yAxis;

	Builder(Sink<HighchartDefinition, HighchartDefinition> sink) {
	    this.sink = sink;
	}
	
	public Number computeCurrentMaxYValue() {	    
	    return series.stream().flatMap(series -> series.getData().stream()).max((a,b) -> Double.compare(a.doubleValue(), b.doubleValue())).get();
	}
	
	public Chart.Builder<Builder> chart() {
	    return new Chart.Builder<>(chart -> {
		Builder.this.chart = chart;
		return Builder.this;
	    });
	}
	
	public Credits.Builder<Builder> credits() {
	    return new Credits.Builder<>(credits -> {
		Builder.this.credits = credits;
		return Builder.this;
	    });
	}
	
	public PlotOptions.Builder<Builder> plotOptions() {
	    return new PlotOptions.Builder<>(plotOptions -> {
		Builder.this.plotOptions = plotOptions;
		return Builder.this;
	    });
	}
	
	public Series.Builder<Builder> series() {
	    return new Series.Builder<>(series -> {
		Builder.this.series.add(series);
		return Builder.this;
	    });
	}
	
	public Title.Builder<Builder> title() {
	    return new Title.Builder<>(title -> {
		Builder.this.title = title;
		return Builder.this;
	    });
	}
	
	public Tooltip.Builder<Builder> tooltip() {
	    return new Tooltip.Builder<>(tooltip -> {
		Builder.this.tooltip = tooltip;
		return Builder.this;
	    });
	}
	
	public Axis.Builder<Builder> xAxis() {
	    return new Axis.Builder<>(xAxis -> {
		Builder.this.xAxis = xAxis;
		return Builder.this;
	    });
	}
	
	public Axis.Builder<Builder> yAxis() {
	    return new Axis.Builder<>(yAxis -> {
		Builder.this.yAxis = yAxis;
		return Builder.this;
	    });
	}

	public HighchartDefinition build() {
	    return this.sink.setObject(
		    new HighchartDefinition(chart, credits, plotOptions, series, title, tooltip, xAxis, yAxis)
	    );
	}
    }
    
    public static Builder define() {
	return new Builder(object -> object);
    }

    private final Chart chart;

    private final Credits credits;

    private final PlotOptions plotOptions;
    
    private final Collection<Series> series;

    private final Title title;

    private final Tooltip tooltip;

    private final Axis xAxis;

    private final Axis yAxis;

    @JsonCreator
    HighchartDefinition(
	    @JsonProperty("chart") Chart chart,
	    @JsonProperty("credits") Credits credits,
	    @JsonProperty("plotOptions") PlotOptions plotOptions,
	    @JsonProperty("series") Collection<Series> series,
	    @JsonProperty("title") Title title,
	    @JsonProperty("tooltip") Tooltip tooltip,
	    @JsonProperty("xAxis") Axis xAxis,
	    @JsonProperty("yAxis") Axis yAxis
    ) {
	this.chart = chart;
	this.credits = credits;
	this.plotOptions = plotOptions;
	this.series = series;
	this.title = title;
	this.tooltip = tooltip;
	this.xAxis = xAxis;
	this.yAxis = yAxis;
    }

    public Chart getChart() {
	return chart;
    }

    public Credits getCredits() {
	return credits;
    }

    public PlotOptions getPlotOptions() {
	return plotOptions;
    }

    public Collection<Series> getSeries() {
	return series;
    }
    
    public Title getTitle() {
	return title;
    }

    public Tooltip getTooltip() {
	return tooltip;
    }

    public Axis getxAxis() {
	return xAxis;
    }

    public Axis getyAxis() {
	return yAxis;
    }
}
