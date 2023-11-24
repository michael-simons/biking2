/*
 * Copyright 2014-2023 michael-simons.eu.
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.test.web.servlet.MockMvc;

import ac.simons.biking2.config.SecurityConfig;
import ac.simons.biking2.shared.TestData;

/**
 * @author Michael J. Simons
 * @since 2014-02-15
 */
@WebMvcTest(
        includeFilters = @Filter(type = ASSIGNABLE_TYPE, value = SecurityConfig.class),
        controllers = ChartsController.class
)
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class ChartsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticService statisticService;

    private final TestData sharedTestData = new TestData();

    @Test
    void emptyCurrentYear() throws Exception {

        CurrentYear currentYear = CurrentYear.builder().build();
        when(statisticService.computeCurrentYear()).thenReturn(currentYear);

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/currentYear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series[0].name", is(equalTo("Sum"))))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)))));

        verify(statisticService).computeCurrentYear();
        verifyNoMoreInteractions(statisticService);
    }

    @Test
    void currentYear() throws Exception {

        var totals = new int[] {10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 30};
        var bike1 = new int[] {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 30};
        var bike2 = new int[] {0, 30, 10, 10, 10, 10, 10, 10, 45, 0, 0, 0};
        var bike3 = new int[] {0, 0, 0, 10, 10, 10, 10, 10, 10, 0, 0, 0};

        Map<Tuple2<String, String>, int[]> values = new LinkedHashMap<>();
        values.put(Tuple.tuple("bike1", "CCC"), bike1);
        values.put(Tuple.tuple("bike2", "CCC"), bike2);
        values.put(Tuple.tuple("bike3", "CCC"), bike3);
        CurrentYear currentYear = CurrentYear.builder()
                .preferredBike("bike1")
                .months(new MonthlyStatistics(totals, values))
                .startOfYear(LocalDate.now().withMonth(1).withDayOfMonth(1))
                .build();
        when(statisticService.computeCurrentYear()).thenReturn(currentYear);

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/currentYear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options.yAxis.max", is(equalTo(65))))
                .andExpect(jsonPath("$.series", hasSize(4)))
                .andExpect(jsonPath("$.series[0].name", is(equalTo("bike1"))))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.stream(bike1).boxed().collect(toList())))))
                .andExpect(jsonPath("$.series[1].name", is(equalTo("bike2"))))
                .andExpect(jsonPath("$.series[1].data", is(equalTo(Arrays.stream(bike2).boxed().collect(toList())))))
                .andExpect(jsonPath("$.series[2].name", is(equalTo("bike3"))))
                .andExpect(jsonPath("$.series[2].data", is(equalTo(Arrays.stream(bike3).boxed().collect(toList())))))
                .andExpect(jsonPath("$.series[3].name", is(equalTo("Sum"))))
                .andExpect(jsonPath("$.series[3].data", is(equalTo(Arrays.stream(totals).boxed().collect(toList())))));

        verify(statisticService).computeCurrentYear();
        verifyNoMoreInteractions(statisticService);
    }

    @Test
    @SuppressWarnings({"unchecked"})
    void emptyMonthlyAverage() throws Exception {

        Map<Integer, MonthlyAverage> monthlyStatistics = new HashMap<>();
        IntStream.rangeClosed(1, 12)
                .forEach(i -> monthlyStatistics.put(i, MonthlyAverage.builder().month(Month.of(i)).build()));
        when(statisticService.computeMonthlyAverage()).thenReturn(monthlyStatistics);

        var listHasSize12 = iterableWithSize(12);
        var listHasSize2 = iterableWithSize(2);
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/monthlyAverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(2)))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)))))
                .andExpect(jsonPath("$.series[1].data", allOf(listHasSize12, everyItem(allOf(listHasSize2, (Matcher) everyItem(is(equalTo(0))))))));

        verify(statisticService).computeMonthlyAverage();
        verifyNoMoreInteractions(statisticService);
    }

    @Test
    void monthlyAverage() throws Exception {

        Map<Integer, MonthlyAverage> monthlyStatistics = new HashMap<>();
        monthlyStatistics.put(1, MonthlyAverage.builder().month(Month.JANUARY).minimum(10).maximum(10).value(10.0).build());
        monthlyStatistics.put(2, MonthlyAverage.builder().month(Month.FEBRUARY).minimum(40).maximum(40).value(40.0).build());
        monthlyStatistics.put(3, MonthlyAverage.builder().month(Month.MARCH).minimum(20).maximum(20).value(20.0).build());
        monthlyStatistics.put(4, MonthlyAverage.builder().month(Month.APRIL).minimum(30).maximum(30).value(30.0).build());
        monthlyStatistics.put(5, MonthlyAverage.builder().month(Month.MAY).minimum(30).maximum(30).value(30.0).build());
        monthlyStatistics.put(6, MonthlyAverage.builder().month(Month.JUNE).minimum(30).maximum(30).value(30.0).build());
        monthlyStatistics.put(7, MonthlyAverage.builder().month(Month.JULY).minimum(30).maximum(30).value(30.0).build());
        monthlyStatistics.put(8, MonthlyAverage.builder().month(Month.AUGUST).minimum(30).maximum(30).value(30.0).build());
        monthlyStatistics.put(9, MonthlyAverage.builder().month(Month.SEPTEMBER).minimum(65).maximum(65).value(65.0).build());
        monthlyStatistics.put(10, MonthlyAverage.builder().month(Month.OCTOBER).minimum(10).maximum(10).value(10.0).build());
        monthlyStatistics.put(11, MonthlyAverage.builder().month(Month.NOVEMBER).minimum(10).maximum(10).value(10.0).build());
        monthlyStatistics.put(12, MonthlyAverage.builder().month(Month.DECEMBER).minimum(30).maximum(30).value(30.0).build());

        when(statisticService.computeMonthlyAverage()).thenReturn(monthlyStatistics);
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/monthlyAverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(2)))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(monthlyStatistics.values().stream().map(MonthlyAverage::getValue).collect(toList())))))
                .andExpect(jsonPath("$.series[1].data", contains(monthlyStatistics.values().stream().map(v -> contains(v.getMinimum(), v.getMaximum())).collect(toList()))));
        verify(statisticService).computeMonthlyAverage();
        verifyNoMoreInteractions(statisticService);
    }

    @Test
    void emptyHistory() throws Exception {

        when(statisticService.computeHistory(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>());

        mockMvc
                .perform(
                        get("http://biking.michael-simons.eu/api/charts/history")
                                .param("start", "47")
                                .param("end", "11")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series").doesNotExist())
                .andExpect(jsonPath("$.userData.worstYear").doesNotExist())
                .andExpect(jsonPath("$.userData.bestYear").doesNotExist())
                .andExpect(jsonPath("$.userData.preferredBikes").doesNotExist());

        verify(statisticService).computeHistory(Optional.of(47), Optional.of(11));
        verifyNoMoreInteractions(statisticService);
    }

    @Test
    void history() throws Exception {

        final LocalDate startDate = sharedTestData.january1st.minusYears(2);
        Map<Integer, HistoricYear> history = new TreeMap<>();
        history.put(startDate.getYear(), HistoricYear.builder()
                .values(new int[] {10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 35})
                .year(startDate.getYear())
                .preferredBike("bike1")
                .build());
        history.put(startDate.getYear() + 1, HistoricYear.builder()
                .values(new int[] {50, 70, 110, 110, 110, 110, 110, 110, 110, 610, 10, 10})
                .year(startDate.getYear() + 1)
                .preferredBike("bike2")
                .build());
        when(statisticService.computeHistory(Optional.empty(), Optional.empty())).thenReturn(history);

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(2)))
                .andExpect(jsonPath("$.series[0].name", is(equalTo(Integer.toString(startDate.getYear())))))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.stream(history.get(startDate.getYear()).getValues()).boxed().collect(toList())))))
                .andExpect(jsonPath("$.series[1].name", is(equalTo(Integer.toString(startDate.getYear() + 1)))))
                .andExpect(jsonPath("$.series[1].data", is(equalTo(Arrays.stream(history.get(startDate.getYear() + 1).getValues()).boxed().collect(toList())))))
                .andExpect(jsonPath("$.userData.worstYear", hasKey(Integer.toString(startDate.getYear()))))
                .andExpect(jsonPath("$.userData.bestYear", hasKey(Integer.toString(startDate.getYear() + 1))))
                .andExpect(jsonPath("$.userData.preferredBikes." + startDate.getYear() + ".name", is(equalTo("bike1"))))
                .andExpect(jsonPath("$.userData.preferredBikes." + (startDate.getYear() + 1) + ".name", is(equalTo("bike2"))));

        verify(statisticService).computeHistory(Optional.empty(), Optional.empty());
        verifyNoMoreInteractions(statisticService);
    }
}
