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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.test.web.servlet.MockMvc;

import ac.simons.biking2.config.SecurityConfig;

/**
 * @author Michael J. Simons
 * @since 2015-12-17
 */
@WebMvcTest(
        includeFilters = @Filter(type = ASSIGNABLE_TYPE, value = SecurityConfig.class),
        controllers = SummaryController.class
)
class SummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticService statisticService;

    @Test
    void summary() throws Exception {

        when(statisticService.computeSummary()).thenReturn(
                Summary.builder()
                        .average(47.11)
                        .bestPeriod(new AccumulatedPeriod(LocalDate.of(2009, 1, 1), 50))
                        .dateOfFirstRecord(LocalDate.of(2009, 1, 1))
                        .total(4711.0)
                        .worstPeriod(new AccumulatedPeriod(LocalDate.of(2009, 2, 1), 43)).build()
        );

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateOfFirstRecord", Matchers.is(equalTo("2009-01-01"))))
                .andExpect(jsonPath("$.total", Matchers.is(equalTo(4711.0))))
                .andExpect(jsonPath("$.average", Matchers.is(equalTo(47.11))))
                .andExpect(jsonPath("$.worstPeriod.startOfPeriod", Matchers.is(equalTo("2009-02-01"))))
                .andExpect(jsonPath("$.worstPeriod.value", Matchers.is(equalTo(43))))
                .andExpect(jsonPath("$.bestPeriod.startOfPeriod", Matchers.is(equalTo("2009-01-01"))))
                .andExpect(jsonPath("$.bestPeriod.value", Matchers.is(equalTo(50))));

        verify(statisticService).computeSummary();
        verifyNoMoreInteractions(statisticService);
    }

    @Test
    void emptySummary() throws Exception {

        when(statisticService.computeSummary()).thenReturn(Summary.builder().build());

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateOfFirstRecord", Matchers.is(nullValue())))
                .andExpect(jsonPath("$.total", Matchers.is(nullValue())))
                .andExpect(jsonPath("$.average", Matchers.is(nullValue())))
                .andExpect(jsonPath("$.worstPeriod", Matchers.is(nullValue())))
                .andExpect(jsonPath("$.bestPeriod", Matchers.is(nullValue())));

        verify(statisticService).computeSummary();
        verifyNoMoreInteractions(statisticService);
    }
}
