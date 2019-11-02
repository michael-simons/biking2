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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.jooq.lambda.tuple.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import ac.simons.biking2.bikes.BikeEntity;
import ac.simons.biking2.bikes.BikeRepository;
import ac.simons.biking2.shared.TestData;

/**
 * @author Michael J. Simons
 * @since 2019-11-01
 */
@DataJpaTest
@ActiveProfiles("test")
// We need fresh data
@TestPropertySource(properties = "spring.datasource.initialization-mode=never")
class StatisticServiceTest {

    private final TestData sharedTestData = new TestData();

    /**
     * Needed for storing the test data.
     */
    @Autowired
    private BikeRepository bikeRepository;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void currentYearWithDataAvailable() {

        // Prepare testdata
        sharedTestData.value.forEach(bikeRepository::save);
        var bike1 = Tuple.tuple("bike1", "CCCCCC");
        var bike2 = Tuple.tuple("bike2", "CCCCCC");
        var bike3 = Tuple.tuple("bike3", "CCCCCC");

        var service = new StatisticService(jdbcTemplate);
        var currentYear = service.computeCurrentYear();
        var months = currentYear.getMonths();

        assertThat(months).isNotNull();
        assertThat(months.getValues()).containsKeys(bike1, bike2, bike3);
        assertThat(months.getValues().get(bike1)).isEqualTo(new int[] {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 30});
        assertThat(months.getValues().get(bike2)).isEqualTo(new int[] {0, 30, 10, 10, 10, 10, 10, 10, 45, 0, 0, 0});
        assertThat(months.getValues().get(bike3)).isEqualTo(new int[] {0, 0, 0, 10, 10, 10, 10, 10, 10, 0, 0, 0});
        assertThat(months.getTotals()).isEqualTo(new int[] {10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 30});
    }

    @Test
    void currentYearNoDataAvailable() {

        var service = new StatisticService(jdbcTemplate);
        var currentYear = service.computeCurrentYear();
        var months = currentYear.getMonths();

        assertThat(currentYear.getYearlyTotal()).isEqualTo(0);
        assertThat(months).isNotNull();
        assertThat(months.getTotals()).isEqualTo(IntStream.generate(() -> 0).limit(12).toArray());
    }

    @Test
    void currentYearNoMilages() {

        bikeRepository.save(new BikeEntity("a bike", LocalDate.now()));

        var service = new StatisticService(jdbcTemplate);
        var currentYear = service.computeCurrentYear();
        var months = currentYear.getMonths();

        assertThat(currentYear.getYearlyTotal()).isEqualTo(0);
        assertThat(months).isNotNull();
        assertThat(months.getTotals()).isEqualTo(IntStream.generate(() -> 0).limit(12).toArray());
    }

    @Test
    void historyNoHistory() {

        // Prepare testdata
        sharedTestData.value.forEach(bikeRepository::save);

        var service = new StatisticService(jdbcTemplate);
        var history = service.computeHistory(Optional.empty(), Optional.empty());

        assertThat(history).isEmpty();
    }

    @Test
    void historyNoDataAvailable() {

        var service = new StatisticService(jdbcTemplate);
        var history = service.computeHistory(Optional.empty(), Optional.empty());

        assertThat(history).isEmpty();
    }

    @Test
    void historyNoMilages() {

        bikeRepository.save(new BikeEntity("a bike", LocalDate.now()));

        var service = new StatisticService(jdbcTemplate);
        var history = service.computeHistory(Optional.empty(), Optional.empty());

        assertThat(history).isEmpty();
    }

    @Test
    void historyWithDataAvailable() {

        final LocalDate startDate = sharedTestData.january1st.minusYears(2);

        prepareTestBikes(startDate).forEach(bikeRepository::save);

        var service = new StatisticService(jdbcTemplate);

        var history = service.computeHistory(Optional.empty(), Optional.empty());
        assertThat(history).containsKeys(startDate.getYear(), startDate.getYear() + 1);
        assertThat(history.get(startDate.getYear()).getYear()).isEqualTo(startDate.getYear());
        assertThat(history.get(startDate.getYear()).getValues()).isEqualTo(new int[] {10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 35});
        assertThat(history.get(startDate.getYear()).getPreferredBike()).isEqualTo("bike1");
        assertThat(history.get(startDate.getYear() + 1).getYear()).isEqualTo(startDate.getYear() + 1);
        assertThat(history.get(startDate.getYear() + 1).getValues()).isEqualTo(new int[] {50, 70, 110, 110, 110, 110, 110, 110, 110, 610, 10, 10});
        assertThat(history.get(startDate.getYear() + 1).getPreferredBike()).isEqualTo("bike2");

        history = service.computeHistory(Optional.empty(), Optional.of(startDate.getYear() + 1));
        assertThat(history).containsOnlyKeys(startDate.getYear());
        assertThat(history.get(startDate.getYear()).getYear()).isEqualTo(startDate.getYear());
        assertThat(history.get(startDate.getYear()).getValues()).isEqualTo(new int[] {10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 35});
        assertThat(history.get(startDate.getYear()).getPreferredBike()).isEqualTo("bike1");

        history = service.computeHistory(Optional.of(startDate.getYear() + 1), Optional.empty());
        assertThat(history).containsOnlyKeys(startDate.getYear() + 1);
        assertThat(history.get(startDate.getYear() + 1).getYear()).isEqualTo(startDate.getYear() + 1);
        assertThat(history.get(startDate.getYear() + 1).getValues()).isEqualTo(new int[] {50, 70, 110, 110, 110, 110, 110, 110, 110, 610, 10, 10});
        assertThat(history.get(startDate.getYear() + 1).getPreferredBike()).isEqualTo("bike2");

        history = service.computeHistory(Optional.of(startDate.getYear() - 4), Optional.of(startDate.getYear() - 2));
        assertThat(history).isEmpty();
    }

    @Test
    void monthlyAverageNoDataAvailable() {

        var service = new StatisticService(jdbcTemplate);
        var monthlyAverage = service.computeMonthlyAverage();

        assertThat(monthlyAverage.size()).isEqualTo(12);
        assertThat(monthlyAverage.values()).allSatisfy(v -> assertThat(v.getValue()).isEqualTo(0.0));
    }

    @Test
    void monthlyAverageNoMilages() {

        bikeRepository.save(new BikeEntity("a bike", LocalDate.now()));

        var service = new StatisticService(jdbcTemplate);
        var monthlyAverage = service.computeMonthlyAverage();

        assertThat(monthlyAverage.size()).isEqualTo(12);
        assertThat(monthlyAverage.values()).allSatisfy(v -> assertThat(v.getValue()).isEqualTo(0.0));
    }

    @Test
    void monthlyAverageWithDataAvailable() {

        final LocalDate startDate = sharedTestData.january1st.minusYears(2);

        prepareTestBikes(startDate).forEach(bikeRepository::save);

        var service = new StatisticService(jdbcTemplate);

        var expectedData = new double[][] {
                {0, 50, (0 + 10 + 50) / 3.0},
                {0, 70, (0 + 40 + 70) / 3.0},
                {10, 110, (10 + 20 + 110) / 3.0},
                {10, 110, (10 + 30 + 110) / 3.0},
                {10, 110, (10 + 30 + 110) / 3.0},
                {10, 110, (10 + 30 + 110) / 3.0},
                {10, 110, (10 + 30 + 110) / 3.0},
                {10, 110, (10 + 30 + 110) / 3.0},
                {10, 110, (10 + 65 + 110) / 3.0},
                {10, 610, (15 + 10 + 610) / 3.0},
                {5, 10, (5 + 10 + 10) / 3.0},
                {10, 35, (0 + 35 + 10) / 2.0}
        };

        assertMonthlyAverage(service.computeMonthlyAverage(), expectedData);
    }

    @Test
    void monthlyAverageNoHistory() {

        sharedTestData.value.forEach(bikeRepository::save);

        var service = new StatisticService(jdbcTemplate);

        var expectedData = new double[][] {
                {10, 10, 10.0},
                {40, 40, 40.0},
                {20, 20, 20.0},
                {30, 30, 30.0},
                {30, 30, 30.0},
                {30, 30, 30.0},
                {30, 30, 30.0},
                {30, 30, 30.0},
                {65, 65, 65.0},
                {10, 10, 10.0},
                {10, 10, 10.0},
                {30, 30, 30.0},
        };

        assertMonthlyAverage(service.computeMonthlyAverage(), expectedData);
    }

    @Test
    void summaryNoDataAvailable() {

        var service = new StatisticService(jdbcTemplate);
        var summary = service.computeSummary();

        assertThat(summary.getBestPeriod()).isNull();
        assertThat(summary.getWorstPeriod()).isNull();

        assertThat(summary.getTotal()).isEqualTo(0);
        assertThat(summary.getAverage()).isEqualTo(0);
    }

    @Test
    void summaryWithDataAvailable() {

        sharedTestData.value.forEach(bikeRepository::save);
        this.jdbcTemplate.update("INSERT INTO assorted_trips (covered_on, distance) values(:covered_on, :distance)", Map.of("covered_on", LocalDate.now(), "distance", BigDecimal.TEN));

        var service = new StatisticService(jdbcTemplate);
        var summary = service.computeSummary();

        assertThat(summary.getBestPeriod().getStartOfPeriod()).isEqualTo(LocalDate.now().withMonth(9).withDayOfMonth(1));
        assertThat(summary.getBestPeriod().getValue()).isEqualTo(65);
        assertThat(summary.getWorstPeriod().getStartOfPeriod()).isEqualTo(LocalDate.now().withMonth(1).withDayOfMonth(1));
        assertThat(summary.getWorstPeriod().getValue()).isEqualTo(10);

        assertThat(summary.getTotal()).isEqualTo(345.0);
        assertThat(summary.getAverage()).isEqualTo(34.5);
    }

    @Test
    void summaryWithPartialDataAvailable() {

        final LocalDate now = LocalDate.now();
        final List<BikeEntity> bikes = List.of(
        // A bike with no milage should not lead to an error
        new BikeEntity("no-milage", now),
        new BikeEntity("some-milage", now)
                .addMilage(LocalDate.of(2009,1,1), 10).getBike()
                .addMilage(LocalDate.of(2009,2,1), 30).getBike()
                .addMilage(LocalDate.of(2009,3,1), 33).getBike(),
        new BikeEntity("more-milage", now)
                .addMilage(LocalDate.of(2009,1,1),  0).getBike()
                .addMilage(LocalDate.of(2009,2,1), 30).getBike()
                .addMilage(LocalDate.of(2009,3,1), 70).getBike()
        );
        bikes.forEach(bikeRepository::save);

        var service = new StatisticService(jdbcTemplate);
        var summary = service.computeSummary();

        assertNotNull(summary.getWorstPeriod());
        assertThat(summary.getWorstPeriod().getStartOfPeriod()).isEqualTo(LocalDate.of(2009,2,1));
        assertThat(summary.getWorstPeriod().getValue()).isEqualTo(43);

        assertNotNull(summary.getBestPeriod());
        assertThat(summary.getBestPeriod().getStartOfPeriod()).isEqualTo(LocalDate.of(2009,1,1));
        assertThat(summary.getBestPeriod().getValue()).isEqualTo(50);
        assertThat(summary.getAverage()).isEqualTo(93.0/Period.between(LocalDate.of(2009,1,1), now).toTotalMonths());
    }

    private static void assertMonthlyAverage(Map<Integer, MonthlyAverage> monthlyAverage, double[][] expectedData) {
        assertThat(monthlyAverage.size()).isEqualTo(12);
        for (int i = 0; i < expectedData.length; ++i) {
            assertThat(monthlyAverage.get(i + 1)).satisfies(v -> {
                        assertThat(v.getMinimum()).isEqualTo((int) expectedData[v.getMonth().getValue() - 1][0]);
                        assertThat(v.getMaximum()).isEqualTo((int) expectedData[v.getMonth().getValue() - 1][1]);
                        assertThat(v.getValue()).isEqualTo(Math.round(expectedData[v.getMonth().getValue() - 1][2]));
                    }
            );
        }
    }

    private static List<BikeEntity> prepareTestBikes(LocalDate startDate) {
        final Map<String, Integer[]> testData = new TreeMap<>();
        testData.put("bike1", new Integer[] {
                10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120,
                150, 200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300,
                310, 310, 310, 320, 330, 340, 350, 360, 370, 380, 395, 400
        });
        testData.put("bike2", new Integer[] {
                0, 0, 30, 40, 50, 60, 70, 80, 90, 135, 135, 135,
                140, 140, 200, 300, 400, 500, 600, 700, 800, 900, 1500, 1500
        });
        testData.put("bike3", new Integer[] {
                null, null, null, 40, 50, 60, 70, 80, 90, 100, null, null
        });

        return testData.entrySet().stream().map(entry -> {
            final BikeEntity bike = new BikeEntity(entry.getKey(), LocalDate.now());
            final Integer[] amounts = entry.getValue();
            for (int i = 0; i < amounts.length; ++i) {
                if (amounts[i] == null) {
                    continue;
                }
                bike.addMilage(startDate.plusMonths(i), amounts[i]);
            }
            return bike;
        }).collect(toList());
    }
}