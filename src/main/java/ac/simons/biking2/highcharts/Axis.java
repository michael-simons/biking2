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
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Michael J. Simons, 2014-02-11
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Axis {

    public static class Builder<PB> {
	private final Sink<PB, Axis> sink;
	
	private Collection<String> categories;

	private Number min;

	private Number max;

	private Number tickInterval;

	private Boolean endOnTick;

	private Title title;	

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
	    return new Title.Builder<>(title -> {
		Builder.this.title = title;
		return Builder.this;
	    });
	}

	public PB build() {
	    return this.sink.setObject(
		    new Axis(categories, endOnTick, max, min, tickInterval, title)
	    );
	}
    }

    private final Collection<String> categories;

    private final Boolean endOnTick;

    private final Number max;

    private final Number min;

    private final Number tickInterval;

    private final Title title;

    @JsonCreator
    Axis(
	    @JsonProperty("categories") Collection<String> categories,
	    @JsonProperty("endOnTick") Boolean endOnTick,
	    @JsonProperty("max") Number max,
	    @JsonProperty("min") Number min,
	    @JsonProperty("tickInterval") Number tickInterval,
	    @JsonProperty("title") Title title
    ) {
	this.categories = categories;
	this.endOnTick = endOnTick;
	this.max = max;
	this.min = min;
	this.tickInterval = tickInterval;
	this.title = title;
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
}
