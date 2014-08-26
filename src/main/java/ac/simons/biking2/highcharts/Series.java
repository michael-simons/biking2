/*
 * Copyright 2014 msimons.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author msimons, 2014-02-12
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Series<T> {

    public static class Builder<PB, T> {

	private final Sink<PB, Series> sink;

	private Collection<T> data;

	private String name;

	private String type;

	private String color;

	public Builder<PB, T> withData(final T... data) {
	    this.data = Arrays.asList(data);
	    return this;
	}
	
	public Builder<PB, T> withData(final int... data) {
	    this.data = (Collection<T>)Arrays.stream(data).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	    return this;
	}
	
	public Builder<PB, T> withName(final String name) {
	    this.name = name;
	    return this;
	}

	public Builder<PB, T> withType(final String type) {
	    this.type = type;
	    return this;
	}

	public Builder<PB, T> withColor(final String color) {
	    this.color = color;
	    return this;
	}

	Builder(Sink<PB, Series> sink) {
	    this.sink = sink;
	}

	public PB build() {
	    return this.sink.setObject(new Series(color, data, name, type));
	}
    }

    private final String color;

    private final Collection<T> data;

    private final String name;

    private final String type;

    public Series(final String color, Collection<T> data, String name, String type) {
	this.color = color;
	this.data = data;
	this.name = name;
	this.type = type;
    }

    public Collection<T> getData() {
	return data;
    }

    public String getName() {
	return name;
    }

    public String getType() {
	return type;
    }

    public String getColor() {
	return color;
    }
}
