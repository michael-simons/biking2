/*
 * Copyright 2014-2017 michael-simons.eu.
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
package ac.simons.biking2.summary;

import ac.simons.biking2.bikes.BikeEntity;
import ac.simons.biking2.trips.AssortedTripRepository;
import ac.simons.biking2.bikes.BikeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Michael J. Simons, 2014-02-17
 */
@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@RequestMapping("/api")
class SummaryController {

    private final BikeRepository bikeRepository;
    private final AssortedTripRepository assortedTripRepository;

    @RequestMapping("/summary")
    public Summary getSummary() {
        final List<BikeEntity> allBikes = this.bikeRepository.findAll();

        final Summary summary = new Summary();
        summary.setDateOfFirstRecord(this.bikeRepository.getDateOfFirstRecord());
        summary.setTotal(allBikes.stream().mapToInt(BikeEntity::getMilage).sum()
                + this.assortedTripRepository.getTotalDistance().doubleValue()
        );

        final Map<LocalDate, Integer> summarizedPeriods = BikeEntity.summarizePeriods(allBikes, null);

        summary.setWorstPeriod(BikeEntity.getWorstPeriod(summarizedPeriods));
        summary.setBestPeriod(BikeEntity.getBestPeriod(summarizedPeriods));
        summary.setAverage(summarizedPeriods.entrySet().stream().mapToInt(Entry::getValue).average().orElseGet(() -> 0.0));

        return summary;
    }
}
