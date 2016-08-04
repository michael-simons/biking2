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
package ac.simons.biking2.bikes;

import ac.simons.biking2.shared.TestData;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.generate;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.MessageSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author Michael J. Simons, 2014-02-15
 */
@RunWith(SpringRunner.class)
@WebMvcTest(
        controllers = ChartsController.class,
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)
        },
        secure = false
)
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
public class ChartsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BikeRepository bikeRepository;

    private final TestData sharedTestData = new TestData();

    @Test
    public void testGetCurrentYearDataAvailable() throws Exception {
        when(bikeRepository.findActive(sharedTestData.cutOffDate)).thenReturn(sharedTestData.value);
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/currentYear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options.yAxis.max", is(equalTo(65))))
                .andExpect(jsonPath("$.series", hasSize(4)))
                .andExpect(jsonPath("$.series[0].name", is(equalTo("bike1"))))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 30)))))
                .andExpect(jsonPath("$.series[1].name", is(equalTo("bike2"))))
                .andExpect(jsonPath("$.series[1].data", is(equalTo(Arrays.asList(0, 30, 10, 10, 10, 10, 10, 10, 45, 0, 0, 0)))))
                .andExpect(jsonPath("$.series[2].name", is(equalTo("bike3"))))
                .andExpect(jsonPath("$.series[2].data", is(equalTo(Arrays.asList(0, 0, 0, 10, 10, 10, 10, 10, 10, 0, 0, 0)))))
                .andExpect(jsonPath("$.series[3].name", is(equalTo("Sum"))))
                .andExpect(jsonPath("$.series[3].data", is(equalTo(Arrays.asList(10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 30)))));
        verify(bikeRepository).findActive(sharedTestData.cutOffDate);
        verifyNoMoreInteractions(bikeRepository);
    }

    @Test
    public void testGetCurrentYearNoData() throws Exception {
        when(bikeRepository.findActive(sharedTestData.cutOffDate)).thenReturn(new ArrayList<>());
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/currentYear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series[0].name", is(equalTo("Sum"))))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(generate(() -> 0).limit(12).collect(ArrayList::new, ArrayList::add, ArrayList::addAll)))));
        verify(bikeRepository).findActive(sharedTestData.cutOffDate);
        verifyNoMoreInteractions(bikeRepository);
    }

    @Test
    public void testGetCurrentYearNoMilages() throws Exception {
        when(bikeRepository.findActive(sharedTestData.cutOffDate)).thenReturn(Arrays.asList(new BikeEntity("bike1", LocalDate.now()), new BikeEntity("bike2", LocalDate.now())));
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/currentYear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series[2].name", is(equalTo("Sum"))))
                .andExpect(jsonPath("$.series[2].data", is(equalTo(generate(() -> 0).limit(12).collect(ArrayList::new, ArrayList::add, ArrayList::addAll)))));
        verify(bikeRepository).findActive(sharedTestData.cutOffDate);
        verifyNoMoreInteractions(bikeRepository);
    }

    @Test
    public void testGetHistoryDataAvailable() throws Exception {
        // Default Testdata has no historical data
        when(bikeRepository.findAll()).thenReturn(sharedTestData.value);
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series").doesNotExist());
        verify(bikeRepository).findAll();
        verifyNoMoreInteractions(bikeRepository);
    }

    @Test
    public void testCompleteData() throws Exception {
        // Arange
        final LocalDate startDate = sharedTestData.january1st.minusYears(2);
        final Calendar _startDate = GregorianCalendar.from(startDate.atStartOfDay(ZoneId.systemDefault()));

        final Map<String, Integer[]> testData = new TreeMap<>();
        testData.put("bike1", new Integer[]{
            10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120,
            150, 200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300,
            310, 310, 310, 320, 330, 340, 350, 360, 370, 380, 395, 400
        //
        });
        testData.put("bike2", new Integer[]{
            0, 0, 30, 40, 50, 60, 70, 80, 90, 135, 135, 135,
            140, 140, 200, 300, 400, 500, 600, 700, 800, 900, 1500, 1500
        });
        testData.put("bike3", new Integer[]{
            null, null, null, 40, 50, 60, 70, 80, 90, 100, null, null
        });

        final List<BikeEntity> bikes = testData.entrySet().stream().map(entry -> {
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

        when(bikeRepository.findActive(GregorianCalendar.from(sharedTestData.january1st.atStartOfDay(ZoneId.systemDefault())))).thenReturn(Arrays.asList(bikes.get(0)));
        when(bikeRepository.findAll()).thenReturn(bikes);
        when(bikeRepository.getDateOfFirstRecord()).thenReturn(_startDate);

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/currentYear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(2)))
                .andExpect(jsonPath("$.series[0].name", is(equalTo("bike1"))))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(0, 0, 10, 10, 10, 10, 10, 10, 10, 15, 5, 0)))))
                .andExpect(jsonPath("$.series[1].name", is(equalTo("Sum"))))
                .andExpect(jsonPath("$.series[1].data", is(equalTo(Arrays.asList(0, 0, 10, 10, 10, 10, 10, 10, 10, 15, 5, 0)))))
                .andExpect(jsonPath("$.userData.worstPeriod.value", is(equalTo(0))))
                .andExpect(jsonPath("$.userData.bestPeriod.value", is(equalTo(15))));

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(2)))
                .andExpect(jsonPath("$.series[0].name", is(equalTo(Integer.toString(startDate.getYear())))))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 35)))))
                .andExpect(jsonPath("$.series[1].name", is(equalTo(Integer.toString(startDate.getYear() + 1)))))
                .andExpect(jsonPath("$.series[1].data", is(equalTo(Arrays.asList(50, 70, 110, 110, 110, 110, 110, 110, 110, 610, 10, 10)))))
                .andExpect(jsonPath("$.userData.worstYear", hasKey(Integer.toString(startDate.getYear()))))
                .andExpect(jsonPath("$.userData.bestYear", hasKey(Integer.toString(startDate.getYear() + 1))))
                .andExpect(jsonPath("$.userData.preferredBikes." + startDate.getYear() + ".name", is(equalTo("bike1"))))
                .andExpect(jsonPath("$.userData.preferredBikes." + (startDate.getYear() + 1) + ".name", is(equalTo("bike2"))));

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/monthlyAverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(2)))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(
                        (0 + 10 + 50) / 3.0,
                        (0 + 40 + 70) / 3.0,
                        (10 + 20 + 110) / 3.0,
                        (10 + 30 + 110) / 3.0,
                        (10 + 30 + 110) / 3.0,
                        (10 + 30 + 110) / 3.0,
                        (10 + 30 + 110) / 3.0,
                        (10 + 30 + 110) / 3.0,
                        (10 + 65 + 110) / 3.0,
                        (15 + 10 + 610) / 3.0,
                        (5 + 10 + 10) / 3.0,
                        (0 + 35 + 10) / 2.0
                )))))
                .andExpect(jsonPath("$.series[1].data", contains(
                        contains(0, 50),
                        contains(0, 70),
                        contains(10, 110),
                        contains(10, 110),
                        contains(10, 110),
                        contains(10, 110),
                        contains(10, 110),
                        contains(10, 110),
                        contains(10, 110),
                        contains(10, 610),
                        contains(5, 10),
                        contains(10, 35)
                )));
    }

    @Test
    public void testCompleteDataFiltered() throws Exception {
        // Arange
        final LocalDate startDate = sharedTestData.january1st.minusYears(2);
        final Calendar _startDate = GregorianCalendar.from(startDate.atStartOfDay(ZoneId.systemDefault()));

        final Map<String, Integer[]> testData = new TreeMap<>();
        testData.put("bike1", new Integer[]{
            10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120,
            150, 200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300,
            310, 310, 310, 320, 330, 340, 350, 360, 370, 380, 395, 400
        //
        });
        testData.put("bike2", new Integer[]{
            0, 0, 30, 40, 50, 60, 70, 80, 90, 135, 135, 135,
            140, 140, 200, 300, 400, 500, 600, 700, 800, 900, 1500, 1500
        });
        testData.put("bike3", new Integer[]{
            null, null, null, 40, 50, 60, 70, 80, 90, 100, null, null
        });

        final List<BikeEntity> bikes = testData.entrySet().stream().map(entry -> {
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

        when(bikeRepository.findActive(GregorianCalendar.from(sharedTestData.january1st.atStartOfDay(ZoneId.systemDefault())))).thenReturn(Arrays.asList(bikes.get(0)));
        when(bikeRepository.findAll()).thenReturn(bikes);
        when(bikeRepository.getDateOfFirstRecord()).thenReturn(_startDate);

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/history").param("end", Integer.toString(startDate.getYear() + 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(1)))
                .andExpect(jsonPath("$.series[0].name", is(equalTo(Integer.toString(startDate.getYear())))))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 35)))))
                .andExpect(jsonPath("$.userData.worstYear").doesNotExist())
                .andExpect(jsonPath("$.userData.bestYear").doesNotExist())
                .andExpect(jsonPath("$.userData.preferredBikes." + startDate.getYear() + ".name", is(equalTo("bike1"))));

        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/history").param("start", Integer.toString(startDate.getYear() + 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(1)))
                .andExpect(jsonPath("$.series[0].name", is(equalTo(Integer.toString(startDate.getYear() + 1)))))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(50, 70, 110, 110, 110, 110, 110, 110, 110, 610, 10, 10)))))
                .andExpect(jsonPath("$.userData.worstYear").doesNotExist())
                .andExpect(jsonPath("$.userData.bestYear").doesNotExist())
                .andExpect(jsonPath("$.userData.preferredBikes." + (startDate.getYear() + 1) + ".name", is(equalTo("bike2"))));

        mockMvc
                .perform(
                        get("http://biking.michael-simons.eu/api/charts/history")
                        .param("start", Integer.toString(startDate.getYear() - 4))
                        .param("end", Integer.toString(startDate.getYear() - 2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series").doesNotExist())
                .andExpect(jsonPath("$.userData.worstYear").doesNotExist())
                .andExpect(jsonPath("$.userData.bestYear").doesNotExist())
                .andExpect(jsonPath("$.userData.preferredBikes").doesNotExist());
    }

    @Test
    public void testGetHistoryNoData() throws Exception {
        when(bikeRepository.findAll()).thenReturn(new ArrayList<>());
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series").doesNotExist());
        verify(bikeRepository).findAll();
        verifyNoMoreInteractions(bikeRepository);
    }

    @Test
    public void testGetHistoryNoMilages() throws Exception {
        when(bikeRepository.findAll()).thenReturn(Arrays.asList(new BikeEntity("bike1", LocalDate.now()), new BikeEntity("bike2", LocalDate.now())));
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series").doesNotExist());
        verify(bikeRepository).findAll();
        verifyNoMoreInteractions(bikeRepository);
    }

    @Test
    public void testGetMonthlyAverage() throws Exception {
        when(bikeRepository.findAll()).thenReturn(sharedTestData.value);
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/monthlyAverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(2)))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(10.0, 40.0, 20.0, 30.0, 30.0, 30.0, 30.0, 30.0, 65.0, 10.0, 10.0, 30.0)))))
                .andExpect(jsonPath("$.series[1].data", contains(
                        contains(10, 10),
                        contains(40, 40),
                        contains(20, 20),
                        contains(30, 30),
                        contains(30, 30),
                        contains(30, 30),
                        contains(30, 30),
                        contains(30, 30),
                        contains(65, 65),
                        contains(10, 10),
                        contains(10, 10),
                        contains(30, 30)
                )));
        verify(bikeRepository).findAll();
        verifyNoMoreInteractions(bikeRepository);
    }

    @Test
    public void testGetMonthlyAverageNoData() throws Exception {
        when(bikeRepository.findAll()).thenReturn(new ArrayList<>());
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/monthlyAverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(2)))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)))))
                .andExpect(jsonPath("$.series[1].data", allOf(iterableWithSize(12), everyItem(allOf(iterableWithSize(2), everyItem(is(equalTo(0))))))));
        verify(bikeRepository).findAll();
        verifyNoMoreInteractions(bikeRepository);
    }

    @Test
    public void testGetMonthlyAverageNoMilages() throws Exception {
        when(bikeRepository.findAll()).thenReturn(Arrays.asList(new BikeEntity("bike1", LocalDate.now()), new BikeEntity("bike2", LocalDate.now())));
        mockMvc
                .perform(get("http://biking.michael-simons.eu/api/charts/monthlyAverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series", hasSize(2)))
                .andExpect(jsonPath("$.series[0].data", is(equalTo(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)))))
                .andExpect(jsonPath("$.series[1].data", allOf(iterableWithSize(12), everyItem(allOf(iterableWithSize(2), everyItem(is(equalTo(0))))))));
        verify(bikeRepository).findAll();
        verifyNoMoreInteractions(bikeRepository);
    }
}
