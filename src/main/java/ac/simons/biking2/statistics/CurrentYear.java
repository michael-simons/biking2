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

import java.time.LocalDate;
import java.util.Collections;

import lombok.Builder;

/**
 * Statistics for the current year.
 *
 * @author Michael J. Simons
 * @since 2019-11-01
 */
@Builder
final class CurrentYear {

    private final LocalDate startOfYear;

    private final MonthlyStatistics months;

    private final int yearlyTotal;

    private final double monthlyAverage;

    private final AccumulatedPeriod worstPeriod;

    private final AccumulatedPeriod bestPeriod;

    private final String preferredBike;

    @SuppressWarnings({"squid:UnusedPrivateMethodUnused"}) // Used by the lombok generated builder.
    private CurrentYear(final LocalDate startOfYear, final MonthlyStatistics months, final int yearlyTotal, final double monthlyAverage, final AccumulatedPeriod worstPeriod, final AccumulatedPeriod bestPeriod, final String preferredBike) {
        this.startOfYear = startOfYear == null ? LocalDate.now().withMonth(1).withDayOfMonth(1) : startOfYear;
        this.months = months == null ? new MonthlyStatistics(new int[12], Collections.emptyMap()) : months;
        this.yearlyTotal = yearlyTotal;
        this.monthlyAverage = monthlyAverage;
        this.worstPeriod = worstPeriod;
        this.bestPeriod = bestPeriod;
        this.preferredBike = preferredBike == null ? "n/a" : preferredBike;
    }

    public LocalDate getStartOfYear() {
        return this.startOfYear;
    }

    public MonthlyStatistics getMonths() {
        return this.months;
    }

    public int getYearlyTotal() {
        return this.yearlyTotal;
    }

    public double getMonthlyAverage() {
        return this.monthlyAverage;
    }

    public AccumulatedPeriod getWorstPeriod() {
        return this.worstPeriod;
    }

    public AccumulatedPeriod getBestPeriod() {
        return this.bestPeriod;
    }

    public String getPreferredBike() {
        return this.preferredBike;
    }
}
