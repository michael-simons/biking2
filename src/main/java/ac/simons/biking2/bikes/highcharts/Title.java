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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Michael J. Simons, 2014-02-11
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Title {

    @SuppressWarnings({"checkstyle:hiddenfield"})
    public static final class Builder<P> {

        private final Sink<P, Title> sink;

        private String text;

        Builder(final Sink<P, Title> sink) {
            this.sink = sink;
        }

        public Builder<P> withText(final String text) {
            this.text = text;
            return this;
        }

        public P build() {
            return this.sink.setObject(new Title(text));
        }
    }

    /**
     * The title of the chart. To disable the title, set the text to null.
     * Defaults to Chart title.
     */
    private final String text;

    @JsonCreator
    Title(@JsonProperty("text") final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
