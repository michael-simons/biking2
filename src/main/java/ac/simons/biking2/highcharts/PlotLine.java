/*
 * Copyright 2014 michael-simons.eu.
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Michael J. Simons, 2014-08-26
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlotLine {

    public static class Builder<PB> {

	private final Sink<PB, PlotLine> sink;

	private Number value;

	private Double width;

	private String color;

	public Builder(Sink<PB, PlotLine> sink) {
	    this.sink = sink;
	}

	public Builder<PB> at(final Number value) {
	    this.value = value;
	    return this;
	}
	
	public Builder<PB> withWidth(final Double lineWidth) {
	    this.width = lineWidth;
	    return this;
	}

	public Builder<PB> withColor(final String lineColor) {
	    this.color = lineColor;
	    return this;
	}

	public PB build() {
	    return this.sink.setObject(
		    new PlotLine(value, width, color)
	    );
	}
    }

    private final Number value;

    private final Double width;

    private final String color;

    public PlotLine(Number value, Double width, String color) {
	this.value = value;
	this.width = width;
	this.color = color;
    }

    public Number getValue() {
	return value;
    }

    public Double getWidth() {
	return width;
    }

    public String getColor() {
	return color;
    }
}
