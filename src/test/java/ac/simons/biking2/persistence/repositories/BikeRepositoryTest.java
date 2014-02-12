/*
 * Copyright 2014 msimons.
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
package ac.simons.biking2.persistence.repositories;

import ac.simons.biking2.persistence.entities.Bike;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This is an integration test of the generated JpaRepositry implementation
 * @author msimons, 2014-02-12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration
public class BikeRepositoryTest {

    @Configuration
    @ComponentScan("ac.simons.biking2.config")
    static class TestConfiguration {
    }

    @Autowired
    private BikeRepository bikeRepository;

    @Test
    public void testFindActive() {
	final LocalDate cutOffDate = LocalDate.of(2014, 1, 1);
	final List<Bike> activeBikes = this.bikeRepository.findActive(Date.from(cutOffDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

	assertThat(activeBikes.size(), is(equalTo(2)));
	assertThat(activeBikes.get(0).getName(), is(equalTo("bike1")));
	assertThat(activeBikes.get(1).getName(), is(equalTo("bike3")));
    }
}
