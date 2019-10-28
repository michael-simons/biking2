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

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Thin abstraction over the database access.
 *
 * @author Michael J. Simons
 * @since 2019-10-28
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class StatisticService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Map<Integer, MonthlyAverage> computeMonthlyAverage() {

        Map<Integer, MonthlyAverage> rv = new HashMap<>(12);
        this.jdbcTemplate.query(
                "with x as (\n"
                        + "    select b.name, m.recorded_on, lead(m.amount) OVER (partition by b.id order by m.recorded_on) - m.amount as p\n"
                        + "    from bikes b join milages m on m.bike_id = b.id\n"
                        + "    order by m.recorded_on asc\n"
                        + " ),\n"
                        + " y as (\n"
                        + "  select x.recorded_on, sum(p) as s\n"
                        + "  from x\n"
                        + " where x.p is not null\n"
                        + "  group by x.recorded_on\n"
                        + "), \n"
                        + "ast as (\n"
                        + "   select dateadd(DAY, -extract(day from a.covered_on) + 1, a.covered_on) as recorded_on, sum(a.distance) as s\n"
                        + "   from assorted_trips a\n"
                        + "   group by recorded_on\n"
                        + " )\n"
                        + "select extract(month from y.recorded_on) as month, \n"
                        + "       round(min(y.s + COALESCE(a.s,0))) as minimum, \n"
                        + "       round(max(y.s + COALESCE(a.s,0))) as maximum, \n"
                        + "       round(avg(y.s + COALESCE(a.s,0))) as average\n"
                        + "from y left outer join ast a on a.recorded_on = y.recorded_on\n"
                        + "group by month order by month asc",
                resultSet -> {
                    var monthNumber = resultSet.getInt("month");
                    var monthlyAverage = MonthlyAverage.builder()
                            .month(Month.of(monthNumber))
                            .minimum(resultSet.getInt("minimum"))
                            .maximum(resultSet.getInt("maximum"))
                            .value(resultSet.getDouble("average"))
                            .build();
                    rv.put(monthNumber, monthlyAverage);
                }
        );

        // Fill up missing months
        IntStream.rangeClosed(1, 12)
                .forEach(i -> rv.putIfAbsent(i, MonthlyAverage.builder().month(Month.of(i)).build()));
        return Collections.unmodifiableMap(rv);
    }

    public Map<Integer, YearlyStatistics> computeHistory(final Optional<Integer> yearStart, final Optional<Integer> yearEnd) {

        var startAndEndParameters = Map.of(
                "start", yearStart.orElse(Integer.MIN_VALUE),
                "end", yearEnd.orElseGet(() -> LocalDate.now().getYear()) - 1);

        Map<Integer, int[]> yearlyValues = new HashMap<>();
        this.jdbcTemplate.query(
                "with x as (\n"
                        + "    select b.name, m.recorded_on, lead(m.amount) OVER (partition by b.id order by m.recorded_on) - m.amount as p\n"
                        + "    from bikes b join milages m on m.bike_id = b.id\n"
                        + "    order by m.recorded_on asc),\n"
                        + "  y as (\n"
                        + "  select x.recorded_on, sum(p) as s\n"
                        + "  from x\n"
                        + "  where x.p is not null\n"
                        + "  group by x.recorded_on\n"
                        + "  )  \n"
                        + "select y.recorded_on, round(sum(y.s)) as v \n"
                        + "from y\n"
                        + "where extract(year from y.recorded_on) between :start and :end\n"
                        + "group by y.recorded_on\n"
                        + "order by y.recorded_on asc\n",
                startAndEndParameters,
                resultSet -> {
                    var recordedOn = resultSet.getDate("recorded_on").toLocalDate();
                    var year = yearlyValues.computeIfAbsent(recordedOn.getYear(), y -> new int[12]);
                    year[recordedOn.getMonthValue() - 1] = resultSet.getInt("v");
                }
        );

        Map<Integer, String> preferredBikes = new HashMap<>();
        this.jdbcTemplate.query(
                "with x as (\n"
                        + "    select b.name, m.recorded_on, lead(m.amount) OVER (partition by b.id order by m.recorded_on) - m.amount as p\n"
                        + "    from bikes b join milages m on m.bike_id = b.id\n"
                        + "    order by m.recorded_on asc\n"
                        + " ),\n"
                        + " y as (\n"
                        + "  select x.name, extract(year from x.recorded_on) as year, sum(p) as s\n"
                        + "  from x\n"
                        + " where x.p is not null\n"
                        + "  group by x.name, year\n"
                        + ") select s.name, s.year from (\n"
                        + "  select y.name, y.year, dense_rank() over (partition by y.year order by max(y.s) desc) as r\n"
                        + "  from y\n"
                        + "  where y.year between :start and :end"
                        + "  group by y.name, y.year\n"
                        + ") s \n"
                        + "where s.r = 1", startAndEndParameters,
                resultSet -> {
                    preferredBikes.putIfAbsent(resultSet.getInt("year"), resultSet.getString("name"));
                }
        );

        return yearlyValues.entrySet().stream()
                .map(e -> YearlyStatistics.builder().year(e.getKey()).values(e.getValue()).preferredBike(preferredBikes.get(e.getKey())).build())
                .collect(Collectors.toMap(YearlyStatistics::getYear, Function.identity()));
    }

    public CurrentYear computeCurrentYear() {

        var startOfYear = LocalDate.now().withMonth(1).withDayOfMonth(1);

        int[] totals = new int[12];
        Arrays.fill(totals, -1);
        Map<Tuple2<String, String>, int[]> values = new LinkedHashMap<>();

        this.jdbcTemplate.query(
                "with x as (\n"
                        + "    select b.name, b.color, m.recorded_on, lead(m.amount) OVER (partition by b.id order by m.recorded_on) - m.amount as p\n"
                        + "    from bikes b join milages m on m.bike_id = b.id\n"
                        + "    where (b.decommissioned_on is null or b.decommissioned_on >= :start_of_year)\n"
                        + "      and extract(year from m.recorded_on) >= extract(year from :start_of_year)\n"
                        + "    order by m.recorded_on asc, b.name\n"
                        + "    )\n"
                        + "select x.name, x.color, extract(month from x.recorded_on) - 1 as idx, x.p, sum(x.p) OVER (partition by x.recorded_on) as total\n"
                        + "from x where p is not null\n"
                        + "order by x.name asc ",
                Map.of("start_of_year", startOfYear),
                resultSet -> {
                    var index = resultSet.getInt("idx");
                    if (totals[index] == -1) {
                        totals[index] = resultSet.getInt("total");
                    }
                    var bikeAndColor = Tuple.tuple(resultSet.getString("name"), resultSet.getString("color"));
                    var milagesInYear = values.computeIfAbsent(bikeAndColor, k -> new int[12]);
                    milagesInYear[index] = resultSet.getInt("p");
                });

        this.jdbcTemplate.query("select extract(month from t.covered_on) - 1 as idx, round(sum(t.distance)) as distance from assorted_trips t\n"
                        + "where t.covered_on >= :start_of_year\n"
                        + "group by extract(month from t.covered_on) - 1",
                Map.of("start_of_year", startOfYear),
                resultSet -> {
                    var index = resultSet.getInt("idx");
                    totals[index] = Math.max(totals[index], 0) + resultSet.getInt("distance");
                }
        );

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
                .monthlyAverage(currentYearSum / Period.between(startOfYear, LocalDate.now()).toTotalMonths())
                .worstPeriod(new AccumulatedPeriod(startOfYear.withMonth(minIndex + 1), minValue))
                .bestPeriod(new AccumulatedPeriod(startOfYear.withMonth(maxIndex + 1), maxValue))
                .preferredBike(preferredBike)
                .build();
    }

    public Summary computeSummary() {

        return this.jdbcTemplate.query("with x as (\n"
                + "    select  b.name, m.recorded_on, lead(m.amount) OVER (partition by b.id order by m.recorded_on) - m.amount as p\n"
                + "    from bikes b join milages m on m.bike_id = b.id\n"
                + "    order by m.recorded_on asc),\n"
                + "  y as (\n"
                + "  select x.recorded_on, sum(p) as v, rank() over (order by sum(p) desc, x.recorded_on desc) as r\n"
                + "  from x\n"
                + "  where x.p is not null\n"
                + "  group by x.recorded_on\n"
                + "), \n"
                + "ast as (\n"
                + "  select sum(distance) v from assorted_trips\n"
                + "),\n"
                + "summary as (\n"
                + "  select min(recorded_on) min_recorded_on, \n"
                + "  sum(y.v) + coalesce(ast.v,0) as s\n"
                + "  from y, ast\n"
                + ") \n"
                + "select\n"
                + "  summary.min_recorded_on as min_recorded_on,\n"
                + "  (summary.s / datediff(MONTH, summary.min_recorded_on, now())) as average,\n"
                + "  summary.s as total,\n"
                + "  b.recorded_on as best_recorded_on,\n"
                + "  b.v as best,\n"
                + "  w.recorded_on as worst_recorded_on,\n"
                + "  w.v as worst\n"
                + "from \n"
                + "  summary,\n"
                + "  (select recorded_on, v from y where y.r = 1) as b,\n"
                + "  (select recorded_on, v from y where y.r = (select max(r) from y)) as w", resultSet -> {

            if (!resultSet.next()) {
                return Summary.builder().total(0.0).average(0.0).build();
            }

            final AccumulatedPeriod worstPeriod = new AccumulatedPeriod(resultSet.getDate("worst_recorded_on").toLocalDate(), resultSet.getInt("worst"));
            final AccumulatedPeriod bestPeriod = new AccumulatedPeriod(resultSet.getDate("best_recorded_on").toLocalDate(), resultSet.getInt("best"));

            return Summary.builder()
                    .worstPeriod(worstPeriod)
                    .bestPeriod(bestPeriod)
                    .average(resultSet.getDouble("average"))
                    .total(resultSet.getDouble("total"))
                    .dateOfFirstRecord(resultSet.getDate("min_recorded_on").toLocalDate())
                    .build();
        });
    }
}
