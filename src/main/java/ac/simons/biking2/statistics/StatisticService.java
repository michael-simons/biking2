/*
 * Copyright 2019-2022 michael-simons.eu.
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

import static ac.simons.biking2.db.Tables.ASSORTED_TRIPS;
import static ac.simons.biking2.db.Tables.BIKES;
import static ac.simons.biking2.db.Tables.MILAGES;
import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.ceil;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.denseRank;
import static org.jooq.impl.DSL.extract;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.lead;
import static org.jooq.impl.DSL.localDateAdd;
import static org.jooq.impl.DSL.localDateDiff;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.min;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.partitionBy;
import static org.jooq.impl.DSL.rank;
import static org.jooq.impl.DSL.round;
import static org.jooq.impl.DSL.sum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Thin abstraction over the database access.
 *
 * @author Michael J. Simons
 * @since 2019-10-28
 */
@Service
@RequiredArgsConstructor
class StatisticService {

    private static final String ALIAS_FOR_VALUE = "value";

    /**
     * The computed monthly milage value.
     */
    private static final Field<BigDecimal> MONTHLY_MILAGE_VALUE =
            lead(MILAGES.AMOUNT).over(partitionBy(BIKES.ID).orderBy(MILAGES.RECORDED_ON)).minus(MILAGES.AMOUNT).as(ALIAS_FOR_VALUE);

    /**
     * The cte for computing the monthly milage value.
     */
    private static final CommonTableExpression<Record5<String, String, Boolean, LocalDate, BigDecimal>> MONTHLY_MILAGES =
            name("monthlyMilages").as(DSL
                    .select(BIKES.NAME, BIKES.COLOR, BIKES.MISCELLANEOUS, MILAGES.RECORDED_ON, MONTHLY_MILAGE_VALUE)
                    .from(BIKES).join(MILAGES).onKey()
                    .orderBy(MILAGES.RECORDED_ON.asc()));

    private final DSLContext database;

    @Cacheable(value = "statistics", key = "#root.methodName")
    public Map<Integer, MonthlyAverage> computeMonthlyAverage() {

        var rv = new HashMap<Integer, MonthlyAverage>(12);

        var aggregatedMonthlyValue = sum(MONTHLY_MILAGES.field(MONTHLY_MILAGE_VALUE)).as(ALIAS_FOR_VALUE);
        var aggregatedMonthlyMilages = name("aggregatedMonthlyMilages").as(DSL
                .select(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON), aggregatedMonthlyValue)
                .from(MONTHLY_MILAGES)
                .where(MONTHLY_MILAGE_VALUE.isNotNull())
                .groupBy(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON)));

        var assortedTripRecordedOn = localDateAdd(
                ASSORTED_TRIPS.COVERED_ON,
                extract(ASSORTED_TRIPS.COVERED_ON, DatePart.DAY).neg().plus(inline(1)),
                DatePart.DAY
        ).as("recorded_on");
        var assortedTripValue = sum(ASSORTED_TRIPS.DISTANCE).as(ALIAS_FOR_VALUE);
        var aggregatedAssortedTrips = name("aggregatedAssortedTrips").as(DSL
                .select(assortedTripRecordedOn, assortedTripValue)
                .from(ASSORTED_TRIPS)
                .groupBy(assortedTripRecordedOn));

        var value = aggregatedMonthlyMilages.field(aggregatedMonthlyValue).plus(coalesce(aggregatedAssortedTrips.field(assortedTripValue), inline(0)));
        var minimum = round(min(value)).as("minimum");
        var maximum = round(max(value)).as("maximum");
        var average = round(avg(value)).as("average");
        var month = extract(aggregatedMonthlyMilages.field(MILAGES.RECORDED_ON), DatePart.MONTH).as("month");
        this.database
                .with(MONTHLY_MILAGES)
                .with(aggregatedMonthlyMilages)
                .with(aggregatedAssortedTrips)
                .select(month, minimum, maximum, average)
                .from(aggregatedMonthlyMilages)
                .leftOuterJoin(aggregatedAssortedTrips)
                .on(aggregatedAssortedTrips.field(assortedTripRecordedOn).eq(aggregatedMonthlyMilages.field(MILAGES.RECORDED_ON)))
                .groupBy(month).orderBy(month.asc())
                .forEach(record -> {
                    var monthNumber = record.get(month).intValue();
                    var monthlyAverage = MonthlyAverage.builder()
                            .month(Month.of(monthNumber))
                            .minimum(record.get(minimum).intValue())
                            .maximum(record.get(maximum).intValue())
                            .value(record.getValue(average).doubleValue())
                            .build();
                    rv.put(monthNumber, monthlyAverage);
                });

        // Fill up missing months
        IntStream.rangeClosed(1, 12)
                .forEach(i -> rv.putIfAbsent(i, MonthlyAverage.builder().month(Month.of(i)).build()));
        return Collections.unmodifiableMap(rv);
    }

    @Cacheable(value = "statistics", key = "#root.methodName+#yearStart+#yearEnd")
    public Map<Integer, HistoricYear> computeHistory(final Optional<Integer> yearStart, final Optional<Integer> yearEnd) {

        var lowerBound = yearStart.orElse(Integer.MIN_VALUE);
        var upperBound = yearEnd.orElseGet(() -> LocalDate.now().getYear()) - 1;

        // Select yearly values
        var yearlyValues = new HashMap<Integer, int[]>();

        var aggregatedMonthlyValue = round(sum(MONTHLY_MILAGE_VALUE));
        this.database
                .with(MONTHLY_MILAGES)
                .select(
                        MONTHLY_MILAGES.field(MILAGES.RECORDED_ON),
                        aggregatedMonthlyValue
                )
                .from(MONTHLY_MILAGES)
                .where(extract(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON), DatePart.YEAR).between(lowerBound).and(upperBound))
                .groupBy(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON))
                .orderBy(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON).asc())
                .forEach(record -> {
                    var recordedOn = record.get(MILAGES.RECORDED_ON);
                    var year = yearlyValues.computeIfAbsent(recordedOn.getYear(), y -> new int[12]);
                    year[recordedOn.getMonthValue() - 1] = record.get(aggregatedMonthlyValue).intValue();
                });

        // Select preferred bikes
        var preferredBikes = new HashMap<Integer, String>();

        var year = extract(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON), DatePart.YEAR).as("year");
        var aggregatedYearlyValue = sum(MONTHLY_MILAGES.field(MONTHLY_MILAGE_VALUE)).as(ALIAS_FOR_VALUE);
        var yearlyMilages = name("yearlyMilages").as(DSL
                .select(MONTHLY_MILAGES.field(BIKES.NAME), year, aggregatedYearlyValue)
                .from(MONTHLY_MILAGES)
                .where(MONTHLY_MILAGE_VALUE.isNotNull())
                .groupBy(MONTHLY_MILAGES.field(BIKES.NAME), year));
        var bikeRank = denseRank().over(partitionBy(yearlyMilages.field(year)).orderBy(max(aggregatedYearlyValue).desc())).as("r");
        var rankedYears = DSL
                .select(
                        yearlyMilages.field(BIKES.NAME),
                        yearlyMilages.field(year),
                        bikeRank
                )
                .from(yearlyMilages)
                .where(yearlyMilages.field(year).between(lowerBound).and(upperBound))
                .groupBy(yearlyMilages.field(BIKES.NAME), yearlyMilages.field(year));
        this.database
                .with(MONTHLY_MILAGES)
                .with(yearlyMilages)
                .select(rankedYears.field(BIKES.NAME), rankedYears.field(year))
                .from(rankedYears)
                .where(bikeRank.eq(inline(1)))
                .forEach(record ->
                        preferredBikes.putIfAbsent(record.get(yearlyMilages.field(year)), record.get(yearlyMilages.field(BIKES.NAME))));

        return yearlyValues.entrySet().stream()
                .map(e -> HistoricYear.builder().year(e.getKey()).values(e.getValue()).preferredBike(preferredBikes.get(e.getKey())).build())
                .collect(Collectors.toMap(HistoricYear::getYear, Function.identity()));
    }

    @Cacheable(value = "statistics", key = "#root.methodName")
    public CurrentYear computeCurrentYear() {

        var startOfYear = LocalDate.now().withMonth(1).withDayOfMonth(1);

        int[] totals = new int[12];
        Arrays.fill(totals, -1);
        Map<Tuple2<String, String>, int[]> values = new LinkedHashMap<>();

        var total = sum(MONTHLY_MILAGES.field(MONTHLY_MILAGE_VALUE)).over(partitionBy(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON))).as("total");
        var idxA = extract(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON), DatePart.MONTH).minus(inline(1)).as("idx");
        this.database
                .with(MONTHLY_MILAGES)
                .select(
                        MONTHLY_MILAGES.field(BIKES.NAME), MONTHLY_MILAGES.field(BIKES.COLOR), MONTHLY_MILAGES.field(BIKES.MISCELLANEOUS),
                        idxA,
                        MONTHLY_MILAGES.field(MONTHLY_MILAGE_VALUE),
                        total
                )
                .from(MONTHLY_MILAGES)
                .where(MONTHLY_MILAGES.field(MONTHLY_MILAGE_VALUE).isNotNull())
                .and(extract(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON), DatePart.YEAR)
                        .greaterOrEqual(extract(startOfYear, DatePart.YEAR)))
                .orderBy(MONTHLY_MILAGES.field(BIKES.NAME).asc())
                .forEach(record -> {
                    var index = record.get(idxA).intValue();
                    if (totals[index] == -1) {
                        totals[index] = record.get(total).intValue();
                    }
                    // Only include non miscellaneous bikes
                    if (!record.get(BIKES.MISCELLANEOUS).booleanValue()) {
                        var bikeAndColor = Tuple.tuple(record.get(BIKES.NAME), record.get(BIKES.COLOR));
                        var milagesInYear = values.computeIfAbsent(bikeAndColor, k -> new int[12]);
                        milagesInYear[index] = record.get(MONTHLY_MILAGE_VALUE).intValue();
                    }
                });

        var idxB = extract(ASSORTED_TRIPS.COVERED_ON, DatePart.MONTH).minus(inline(1)).as("idx");
        this.database
                .select(idxB, round(sum(ASSORTED_TRIPS.DISTANCE)).as("distance"))
                .from(ASSORTED_TRIPS)
                .where(ASSORTED_TRIPS.COVERED_ON.greaterOrEqual(startOfYear))
                .groupBy(idxB)
                .forEach(record -> {
                    var index = record.component1();
                    totals[index] = Math.max(totals[index], 0) + record.component2().intValue();
                });

        var maxValue = Integer.MIN_VALUE;
        var minValue = Integer.MAX_VALUE;
        var maxIndex = 0;
        var minIndex = 0;

        for (int i = 0; i < totals.length; ++i) {
            int currentValue = totals[i];
            if (currentValue == -1) {
                totals[i] = 0;
            } else {
                if (currentValue > maxValue) {
                    maxValue = currentValue;
                    maxIndex = i;
                }
                if (currentValue < minValue) {
                    minValue = currentValue;
                    minIndex = i;
                }
            }
        }

        var preferredBike = values.entrySet().stream()
                .map(entry -> Tuple.tuple(entry.getKey().v1, Arrays.stream(entry.getValue()).sum()))
                .max(Comparator.comparing(Tuple2::v2))
                .map(Tuple2::v1)
                .orElse("n/a");

        var currentYearSum = Arrays.stream(totals).sum();
        return CurrentYear.builder()
                .startOfYear(startOfYear)
                .months(new MonthlyStatistics(totals, values))
                .yearlyTotal(currentYearSum)
                .monthlyAverage((double) currentYearSum / Period.between(startOfYear, LocalDate.now().plusMonths(1).withDayOfMonth(1)).toTotalMonths())
                .worstPeriod(new AccumulatedPeriod(startOfYear.withMonth(minIndex + 1), minValue))
                .bestPeriod(new AccumulatedPeriod(startOfYear.withMonth(maxIndex + 1), maxValue))
                .preferredBike(preferredBike)
                .build();
    }

    @Cacheable(value = "statistics", key = "#root.methodName")
    public Summary computeSummary() {

        var aggregatedMonthlyValue = sum(MONTHLY_MILAGES.field(MONTHLY_MILAGE_VALUE)).as(ALIAS_FOR_VALUE);
        var monthRank = rank().over().orderBy(sum(MONTHLY_MILAGES.field(MONTHLY_MILAGE_VALUE)).desc(), MONTHLY_MILAGES.field(MILAGES.RECORDED_ON).desc()).as("month_rank");
        var aggregatedMonthlyMilages = name("aggregatedMonthlyMilages").as(DSL
                .select(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON), aggregatedMonthlyValue, monthRank)
                .from(MONTHLY_MILAGES)
                .where(MONTHLY_MILAGE_VALUE.isNotNull())
                .groupBy(MONTHLY_MILAGES.field(MILAGES.RECORDED_ON)));

        var aggregatedTripsValue = sum(ASSORTED_TRIPS.DISTANCE).as(ALIAS_FOR_VALUE);
        var aggregatedTrips = name("aggregatedTrips").as(DSL
                .select(aggregatedTripsValue)
                .from(ASSORTED_TRIPS));

        var minPeriod = min(aggregatedMonthlyMilages.field(MILAGES.RECORDED_ON)).as("min_period");
        var summaryValue = sum(aggregatedMonthlyMilages.field(aggregatedMonthlyValue)).plus(coalesce(aggregatedTrips.field(aggregatedTripsValue), inline(0))).as("summaryValue");
        var summary = name("summary").as(DSL
                .select(minPeriod, summaryValue)
                .from(aggregatedMonthlyMilages, aggregatedTrips)
        );

        var bestPeriod = DSL
                .select(
                        aggregatedMonthlyMilages.field(MILAGES.RECORDED_ON),
                        aggregatedMonthlyMilages.field(aggregatedMonthlyValue))
                .from(aggregatedMonthlyMilages)
                .where(aggregatedMonthlyMilages.field(monthRank).eq(inline(1)))
                .asTable("bestPeriod");
        var worstPeriod = DSL
                .select(
                        aggregatedMonthlyMilages.field(MILAGES.RECORDED_ON),
                        aggregatedMonthlyMilages.field(aggregatedMonthlyValue))
                .from(aggregatedMonthlyMilages)
                .where(aggregatedMonthlyMilages.field(monthRank).eq(DSL.select(max(aggregatedMonthlyMilages.field(monthRank))).from(aggregatedMonthlyMilages)))
                .asTable("worstPeriod");

        var bestPeriodRecordedOn = bestPeriod.field(MILAGES.RECORDED_ON);
        var bestPeriodValue = bestPeriod.field(aggregatedMonthlyValue);
        var worstPeriodRecordedOn = worstPeriod.field(MILAGES.RECORDED_ON);
        var worstPeriodValue = worstPeriod.field(aggregatedMonthlyValue);
        var dateDiff = localDateDiff(DSL.currentLocalDate(), summary.field(minPeriod));
        var average = DSL.if_(dateDiff.eq(0), inline(Double.POSITIVE_INFINITY), summary.field(summaryValue).div(ceil(dateDiff.div(inline(30.4167)))))
            .cast(Double.class)
            .as("average");
        return this.database
                .with(MONTHLY_MILAGES)
                .with(aggregatedMonthlyMilages)
                .with(aggregatedTrips)
                .with(summary)
                .select(
                        summary.field(minPeriod), average, summary.field(summaryValue),
                        bestPeriodRecordedOn,
                        bestPeriodValue,
                        worstPeriodRecordedOn,
                        worstPeriodValue
                )
                .from(summary, bestPeriod, worstPeriod)
                .fetchOptional()
                .map(record -> Summary.builder()
                        .worstPeriod(new AccumulatedPeriod(record.get(worstPeriodRecordedOn), record.get(worstPeriodValue).intValue()))
                        .bestPeriod(new AccumulatedPeriod(record.get(bestPeriodRecordedOn), record.get(bestPeriodValue).intValue()))
                        .average(record.get(average))
                        .total(record.get(summaryValue).doubleValue())
                        .dateOfFirstRecord(record.get(minPeriod))
                        .build()
                ).orElse(Summary.builder().total(0.0).average(0.0).build());
    }
}
