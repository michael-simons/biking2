/*
 * Copyright 2016 michael-simons.eu.
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
package ac.simons.biking2.shared;

import ac.simons.biking2.bikes.BikeEntity;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author Michael J. Simons, 2016-08-04
 */
public class TestData {

    public final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);
    public final GregorianCalendar cutOffDate = GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault()));

    public final List<BikeEntity> value;

    public TestData() {
        final Map<String, Integer[]> testData = new TreeMap<>();
        testData.put("bike1", new Integer[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 150});
        testData.put("bike2", new Integer[]{0, 0, 30, 40, 50, 60, 70, 80, 90, 135, 135, 135});
        testData.put("bike3", new Integer[]{null, null, null, 40, 50, 60, 70, 80, 90, 100, null, null});

        this.value = testData.entrySet().stream().map(entry -> {
            final BikeEntity bike = new BikeEntity(entry.getKey(), LocalDate.now());
            final Integer[] amounts = entry.getValue();
            for (int i = 0; i < amounts.length; ++i) {
                if (amounts[i] == null) {
                    continue;
                }
                bike.addMilage(january1st.plusMonths(i), amounts[i]);
            }
            return bike;
        }).collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
