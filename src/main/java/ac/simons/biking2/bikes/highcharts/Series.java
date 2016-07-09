/*
 * Copyright 2014-2016 Michael J. Simons.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Michael J. Simons., 2014-02-12
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Series<T> {

    public static final class Builder<PB, T> {

        private final Sink<PB, Series> sink;

        private Collection<T> data;

        private String name;

        private String type;

        private String color;

        private Double fillOpacity;

        private Double lineWidth;

        private String linkedTo;

        private Integer zIndex;

        private Marker marker;

        public Builder<PB, T> withData(final T... data) {
            this.data = Arrays.asList(data);
            return this;
        }

        public Builder<PB, T> withData(final int... data) {
            this.data = (Collection<T>) Arrays.stream(data).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
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

        public Builder<PB, T> withLineWidth(final Double lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public Builder<PB, T> withFillOpacity(final Double fillOpacity) {
            this.fillOpacity = fillOpacity;
            return this;
        }

        public Builder<PB, T> withZIndex(final Integer zIndex) {
            this.zIndex = zIndex;
            return this;
        }

        public Builder<PB, T> linkTo(final String linkTo) {
            this.linkedTo = linkTo;
            return this;
        }

        public Marker.Builder<Builder<PB, T>> marker() {
            return new Marker.Builder<>(marker -> {
                Builder.this.marker = marker;
                return Builder.this;
            });
        }

        Builder(final Sink<PB, Series> sink) {
            this.sink = sink;
        }

        public PB build() {
            return this.sink.setObject(new Series(color, data, name, type, zIndex, fillOpacity, lineWidth, linkedTo, marker));
        }
    }

    private final String color;

    private final Collection<T> data;

    private final String name;

    private final String type;

    private final Integer zIndex;

    private final Double fillOpacity;

    private final Double lineWidth;

    private final String linkedTo;

    private final Marker marker;

    public Series(
            final String color,
            final Collection<T> data,
            final String name,
            final String type,
            final Integer zIndex,
            final Double fillOpacity,
            final Double lineWidth,
            final String linkedTo,
            final Marker marker
    ) {
        this.color = color;
        this.data = data;
        this.name = name;
        this.type = type;
        this.zIndex = zIndex;
        this.fillOpacity = fillOpacity;
        this.lineWidth = lineWidth;
        this.linkedTo = linkedTo;
        this.marker = marker;
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

    public Integer getzIndex() {
        return zIndex;
    }

    public String getColor() {
        return color;
    }

    public Double getFillOpacity() {
        return fillOpacity;
    }

    public Double getLineWidth() {
        return lineWidth;
    }

    public String getLinkedTo() {
        return linkedTo;
    }

    public Marker getMarker() {
        return marker;
    }
}
