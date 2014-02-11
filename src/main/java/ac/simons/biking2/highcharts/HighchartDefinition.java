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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Michael J. Simons, 2014-02-11
 */
public class HighchartDefinition {
    public static class Builder<PB> {

	private final Sink<PB, HighchartDefinition> sink;

	private Chart chart;

	private Credits credits;

	private PlotOptions plotOptions;

	private Title title;

	private Tooltip tooltip;

	private Axis xAxis;

	private Axis yAxis;

	Builder(Sink<PB, HighchartDefinition> sink) {
	    this.sink = sink;
	}
	
	public Chart.Builder<Builder<PB>> chart() {
	    return new Chart.Builder<>(chart -> {
		Builder.this.chart = chart;
		return Builder.this;
	    });
	}
	
	public Credits.Builder<Builder<PB>> credits() {
	    return new Credits.Builder<>(credits -> {
		Builder.this.credits = credits;
		return Builder.this;
	    });
	}
	
	public PlotOptions.Builder<Builder<PB>> plotOptions() {
	    return new PlotOptions.Builder<>(plotOptions -> {
		Builder.this.plotOptions = plotOptions;
		return Builder.this;
	    });
	}
	
	public Title.Builder<Builder<PB>> title() {
	    return new Title.Builder<>(title -> {
		Builder.this.title = title;
		return Builder.this;
	    });
	}
	
	public Tooltip.Builder<Builder<PB>> tooltip() {
	    return new Tooltip.Builder<>(tooltip -> {
		Builder.this.tooltip = tooltip;
		return Builder.this;
	    });
	}
	
	public Axis.Builder<Builder<PB>> xAxis() {
	    return new Axis.Builder<>(xAxis -> {
		Builder.this.xAxis = xAxis;
		return Builder.this;
	    });
	}
	
	public Axis.Builder<Builder<PB>> yAxis() {
	    return new Axis.Builder<>(yAxis -> {
		Builder.this.yAxis = yAxis;
		return Builder.this;
	    });
	}

	public PB build() {
	    return this.sink.setObject(
		    new HighchartDefinition(chart, credits, plotOptions, title, tooltip, xAxis, yAxis)
	    );
	}
    }
    
    public static Builder<HighchartDefinition> define() {
	return new Builder<>(object -> object);
    }

    private final Chart chart;

    private final Credits credits;

    private final PlotOptions plotOptions;

    private final Title title;

    private final Tooltip tooltip;

    private final Axis xAxis;

    private final Axis yAxis;

    @JsonCreator
    HighchartDefinition(
	    @JsonProperty("chart") Chart chart,
	    @JsonProperty("credits") Credits credits,
	    @JsonProperty("plotOptions") PlotOptions plotOptions,
	    @JsonProperty("title") Title title,
	    @JsonProperty("tooltip") Tooltip tooltip,
	    @JsonProperty("xAxis") Axis xAxis,
	    @JsonProperty("yAxis") Axis yAxis
    ) {
	this.chart = chart;
	this.credits = credits;
	this.plotOptions = plotOptions;
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
