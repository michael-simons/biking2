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

import static java.util.stream.Collectors.summarizingDouble;
import static java.util.stream.IntStream.rangeClosed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ac.simons.biking2.statistics.highcharts.HighchartsNgConfig;

/**
 * @author Michael J. Simons, 2014-02-09
 */
@RestController
@RequestMapping("/api")
class ChartsController {

    enum Messages {

        MILAGE_KM("milageKm"),
        TITLE_MONTHLY_AVERAGE("titleMonthlyAverage"),
        TITLE_CURRENT_YEAR("titleCurrentYear");

        public final String key;

        Messages(final String key) {
            this.key = "statistics." + key;
        }
    }

    private final StatisticService statisticService;

    private final String colorOfCumulativeGraph;

    private final MessageSourceAccessor i18n;

    ChartsController(
            final StatisticService statisticService,
            @Value("${biking2.color-of-cumulative-graph:000000}") final String colorOfCumulativeGraph,
            final MessageSource messageSource
    ) {
        this.statisticService = statisticService;
        this.colorOfCumulativeGraph = colorOfCumulativeGraph;
        this.i18n = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
    }

    @RequestMapping("/charts/currentYear")
    public HighchartsNgConfig getCurrentYear() {
        // Start of current year
        final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

        // Get statistics
        final CurrentYear currentYear = this.statisticService.computeCurrentYear();

        final Map<String, Object> userData = new HashMap<>();
        userData.put("worstPeriod", currentYear.getWorstPeriod());
        userData.put("bestPeriod", currentYear.getBestPeriod());
        userData.put("average", currentYear.getMonthlyAverage());
        userData.put("preferredBike", Map.of("name", currentYear.getPreferredBike()));
        userData.put("currentYear", currentYear.getStartOfYear().getYear());
        userData.put("currentYearSum", currentYear.getYearlyTotal());

        final HighchartsNgConfig.Builder builder = HighchartsNgConfig.define();
        builder.withUserData(userData);

        // Add the bike charts as columns
        currentYear.getMonths().getValues().forEach((bikeAndColor, milagesInYear) -> {
            builder.series()
                    .withName(bikeAndColor.v1)
                    .withColor("#" + bikeAndColor.v2)
                    .withType("column")
                    .withData(milagesInYear)
                    .build();
        });

        // Add sum as spline and compute maximum y value
        final int currentMaxYValue =
                builder.series()
                        .withName("Sum")
                        .withColor("#" + colorOfCumulativeGraph)
                        .withType("spline")
                        .withData(currentYear.getMonths().getTotals())
                        .build()
                        .computeCurrentMaxYValue().intValue();

        final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        return builder
                .options()
                    .chart()
                        .withBorderWidth(1)
                        .build()
                    .credits()
                        .disable()
                        .build()
                    .title()
                        .withText(i18n.getMessage(Messages.TITLE_CURRENT_YEAR.key, new Object[]{january1st.getYear()}))
                        .build()
                    .xAxis()
                        .withCategories(
                            rangeClosed(1, 12).mapToObj(i -> january1st.withMonth(i).format(dateTimeFormat)).toArray(size -> new String[size])
                        )
                        .build()
                    .yAxis()
                        .withMin(0)
                        .withMax(currentMaxYValue)
                        .withTickInterval(100)
                        .enableEndOnTick()
                        .title()
                            .withText(i18n.getMessage(Messages.MILAGE_KM.key))
                            .build()
                        .build()
                    .tooltip()
                        .withHeaderFormat("{point.key}<table class=\"table table-condensed table-striped\">")
                        .withPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td><td style=\"padding:0\"><b>{point.y:.1f} km</b></td></tr>")
                        .withFooterFormat("</table>")
                        .enableCrosshairs()
                        .share()
                        .useHTML()
                        .build()
                    .plotOptions()
                        .column()
                            .withPointPadding(0.2)
                            .withBorderWidth(0)
                            .build()
                        .series()
                            .disableAnimation()
                            .build()
                        .build()
                    .build()
                .build();
    }

    @RequestMapping("/charts/history")
    public HighchartsNgConfig getHistory(
            @RequestParam(value = "start") final Optional<Integer> yearStart,
            @RequestParam(value = "end") final Optional<Integer> yearEnd
    ) {
        final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

        final Map<String, Object> userData = new HashMap<>();
        final HighchartsNgConfig.Builder builder = HighchartsNgConfig.define();

        // Get statistics and create series in builder
        final Map<Integer, YearlyStatistics> history = this.statisticService.computeHistory(yearStart, yearEnd);
        history.forEach((k, v) -> builder.series().withName(Integer.toString(k)).withData(v.getValues()).build());

        final StringBuilder title = new StringBuilder();
        if (history.isEmpty()) {
            title.append("No historical data available.");
        } else {
            // Compute summed years
            final Map<Integer, Integer> summedYears = history.values().stream()
                    .collect(Collectors.toMap(YearlyStatistics::getYear, YearlyStatistics::getYearlyTotal));

            // Computed worst and best year from summed years
            final Optional<Map.Entry<Integer, Integer>> worstYear = summedYears.entrySet().stream().min(Map.Entry.comparingByValue());
            final Optional<Map.Entry<Integer, Integer>> bestYear = summedYears.entrySet().stream().max(Map.Entry.comparingByValue());
            // Only add them if they differ
            if (!worstYear.equals(bestYear)) {
                userData.put("worstYear", worstYear.orElse(null));
                userData.put("bestYear", bestYear.orElse(null));
            }

            // Preferred bikes...
            userData.put("preferredBikes",
                    history.values().stream()
                            .collect(Collectors.toMap(YearlyStatistics::getYear, y -> Map.of("name", y.getPreferredBike())))

            );

            title.append("Milage ")
                    .append(history.keySet().stream().min(Integer::compare).get())
                    .append("-")
                    .append(history.keySet().stream().max(Integer::compare).get());
        }

        final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        return builder
                .withUserData(userData)
                .options()
                    .chart()
                        .withBorderWidth(1)
                        .withType("line")
                        .build()
                    .credits()
                        .disable()
                        .build()
                    .title()
                        .withText(title.toString())
                        .build()
                    .xAxis()
                        .withCategories(
                            rangeClosed(1, 12).mapToObj(i -> january1st.withMonth(i).format(dateTimeFormat)).toArray(size -> new String[size])
                        )
                        .build()
                    .yAxis()
                        .withMin(0)
                        .withTickInterval(100)
                        .enableEndOnTick()
                        .title()
                            .withText(i18n.getMessage(Messages.MILAGE_KM.key))
                            .build()
                        .build()
                    .tooltip()
                        .withHeaderFormat("{point.key}<table class=\"table table-condensed table-striped\">")
                        .withPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td><td style=\"padding:0\"><b>{point.y:.1f} km</b></td></tr>")
                        .withFooterFormat("</table>")
                        .enableCrosshairs()
                        .share()
                        .useHTML()
                        .build()
                    .plotOptions()
                        .column()
                            .withPointPadding(0.2)
                            .withBorderWidth(0)
                            .build()
                        .series()
                            .disableAnimation()
                            .build()
                        .build()
                    .build()
                .build();
    }

    @RequestMapping("/charts/monthlyAverage")
    public HighchartsNgConfig getMonthlyAverage() {

        // Get statistics
        final Map<Integer, MonthlyAverage> monthlyAverages = this.statisticService.computeMonthlyAverage();

        // Create stupid arrays from statistics...
        final Double[] averages = new Double[12];
        final Integer[][] ranges = new Integer[12][];
        Arrays.setAll(averages, i -> monthlyAverages.get(i + 1).getValue());
        Arrays.setAll(ranges, i -> {
            var monthlyAverage = monthlyAverages.get(i + 1);
            return new Integer[] {monthlyAverage.getMinimum(), monthlyAverage.getMaximum()};
        });

        // Total average as plotline
        double totalMonthlyAverage =
                monthlyAverages.values().stream().collect(summarizingDouble(MonthlyAverage::getValue)).getAverage();

        final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);
        final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        final String labelColor = "#7CB5EB";
        final HighchartsNgConfig.Builder builder = HighchartsNgConfig.define()
                .options()
                    .chart()
                        .withBorderWidth(1)
                        .withType("line")
                        .build()
                    .credits()
                        .disable()
                        .build()
                    .title()
                        .withText(i18n.getMessage(Messages.TITLE_MONTHLY_AVERAGE.key))
                        .build()
                    .xAxis()
                        .withCategories(
                            rangeClosed(1, 12).mapToObj(i -> january1st.withMonth(i).format(dateTimeFormat)).toArray(size -> new String[size])
                        )
                        .build()
                    .yAxis()
                        .withMin(0)
                        .withTickInterval(100)
                        .withPlotLine()
                            .at(totalMonthlyAverage)
                            .withWidth(2.0)
                            .withColor("#F04124")
                            .build()
                        .enableEndOnTick()
                        .title()
                            .withText(i18n.getMessage(Messages.MILAGE_KM.key))
                            .build()
                        .build()
                    .plotOptions()
                        .column()
                            .withPointPadding(0.2)
                            .withBorderWidth(0)
                            .build()
                        .series()
                            .disableAnimation()
                            .build()
                        .build()
                    .tooltip()
                        .share()
                        .enableCrosshairs()
                        .withValueSuffix("km")
                        .build()
                    .build()
                .<Number>series()
                    .withName("Average")
                    .withColor(labelColor)
                    .withZIndex(1)
                    .marker()
                        .withLineWidth(2.0)
                        .withFillColor("#FFFFFF")
                        .withLineColor(labelColor)
                        .build()
                    .withData(averages)
                    .build()
                .<Number[]>series()
                    .withName("Range")
                    .withType("arearange")
                    .withLineWidth(0.0)
                    .withFillOpacity(0.3)
                    .withColor(labelColor)
                    .withZIndex(0)
                    .linkTo("previous")
                    .withData(ranges)
                    .build();

        return builder.build();
    }
}
