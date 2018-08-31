/*
 * Copyright 2014-2018 michael-simons.eu.
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
package ac.simons.biking2.trips;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Michael J. Simons, 2014-02-08
 */
public interface AssortedTripRepository extends JpaRepository<AssortedTripEntity, Integer> {

    @Query("Select coalesce(sum(t.distance), 0) as totalDistance from AssortedTripEntity t")
    BigDecimal getTotalDistance();

    @Query("Select coalesce(sum(t.distance), 0) as totalDistance from AssortedTripEntity t where extract(year from t.coveredOn) = :year")
    BigDecimal getTotalDistanceInYear(Integer year);
}
