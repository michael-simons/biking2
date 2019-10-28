/*
 * Copyright 2014-2019 michael-simons.eu.
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

import java.util.Arrays;

import lombok.Builder;

/**
 * Statistics for a year.
 *
 * @author Michael J. Simons
 * @since 2019-11-01
 */
final class YearlyStatistics {

    private final int year;

    private final int[] values;

    private final int yearlyTotal;

    private final String preferredBike;

    @Builder
    private YearlyStatistics(final int year, final int[] values, final String preferredBike) {
        this.year = year;
        this.values = values;
        this.yearlyTotal = Arrays.stream(values).sum();
        this.preferredBike = preferredBike;
    }

    public int getYear() {
        return year;
    }

    public int[] getValues() {
        return values;
    }

    public int getYearlyTotal() {
        return yearlyTotal;
    }

    public String getPreferredBike() {
        return preferredBike;
    }
}
