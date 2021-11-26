/*
 * Copyright 2014-2021 michael-simons.eu.
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * @author Michael J. Simons
 *
 * @since 2014-02-08
 */
public interface BikeRepository extends Repository<BikeEntity, Integer> {

    @Query(value = """
        Select b from BikeEntity b
         where b.decommissionedOn is null
            or b.decommissionedOn >= :cutoffDate
         order by b.name asc
        """
    )
    List<BikeEntity> findActive(LocalDate cutoffDate);

    BikeEntity findByName(String name);

    List<BikeEntity> findByDecommissionedOnIsNull(Sort sort);

    List<BikeEntity> findAll(Sort sort);

    Optional<BikeEntity> findById(Integer id);

    BikeEntity save(BikeEntity bike);
}
