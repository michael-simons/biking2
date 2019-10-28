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
package ac.simons.biking2.bikes;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This is an integration test of the generated JpaRepositry implementation
 *
 * @author Michael J. Simons
 * @since 2014-02-12
 */
@DataJpaTest
@ActiveProfiles("test")
class BikeRepositoryTest {

    @Autowired
    private BikeRepository bikeRepository;

    @Test
    @Transactional
    @Rollback
    void nameShouldBeUnique() {
        // There's a bike1 in the test data
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> this.bikeRepository.save(new BikeEntity("bike1", LocalDate.now())));
    }

    @Test
    void testFindActive() {
        final LocalDate cutOffDate = LocalDate.of(2014, 1, 1);
        final List<BikeEntity> activeBikes = this.bikeRepository.findActive(cutOffDate);

        assertThat(activeBikes.size()).isEqualTo(3);
        assertThat(activeBikes.get(0).getName()).isEqualTo("bike1");
        assertThat(activeBikes.get(1).getName()).isEqualTo("bike3");
    }

    @Test
    void testFindByName() {
        final BikeEntity bike = this.bikeRepository.findByName("bike1");
        assertThat(bike).isNotNull();
    }

    /**
     * This actually doesn't test the repository but the mapping, but i wanted to
     * double check if i got the bidirectional mapping right this time. A milage
     * is managed by a bike.
     *
     * @throws SQLException
     */
    @Test
    void testAddMilage(@Autowired NamedParameterJdbcTemplate jdbcTemplate) {
        final BikeEntity bike = this.bikeRepository.findByName("testAddMilageBike");

        bike.addMilage(LocalDate.now(), 23).getBike()
                .addMilage(LocalDate.now().plusMonths(1).withDayOfMonth(1), 42);
        this.bikeRepository.save(bike);

        int cnt = jdbcTemplate.queryForObject("Select count(*) as cnt from milages where bike_id = :bike_id", Collections.singletonMap("bike_id", bike.getId()), Integer.class);
        assertThat(cnt).isEqualTo(2);
    }
}
