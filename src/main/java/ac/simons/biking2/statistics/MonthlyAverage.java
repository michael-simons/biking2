/*
 * Copyright 2019 michael-simons.eu.
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
package ac.simons.biking2.statistics;

import java.time.Month;

import lombok.Builder;

/**
 * Represents monthly average for a single month.
 *
 * @author Michael J. Simons
 * @since 2019-10-28
 */
@Builder
final class MonthlyAverage {

    private final Month month;

    private final int minimum;

    private final int maximum;

    /**
     * The actual average value
     */
    private final double value;

    @SuppressWarnings({"squid:UnusedPrivateMethodUnused"}) // Used by the lombok generated builder.
    private MonthlyAverage(final Month month, final int minimum, final int maximum, final double value) {
        this.month = month;
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = value;
    }

    public Month getMonth() {
        return this.month;
    }

    public int getMinimum() {
        return this.minimum;
    }

    public int getMaximum() {
        return this.maximum;
    }

    public double getValue() {
        return this.value;
    }
}
