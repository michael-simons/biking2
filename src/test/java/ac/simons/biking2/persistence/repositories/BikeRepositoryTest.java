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

import ac.simons.biking2.config.TestConfig;
import ac.simons.biking2.persistence.entities.Bike;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.sql.DataSource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This is an integration test of the generated JpaRepositry implementation
 *
 * @author msimons, 2014-02-12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
public class BikeRepositoryTest {

    @Autowired
    private BikeRepository bikeRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    public void testFindActive() {
	final LocalDate cutOffDate = LocalDate.of(2014, 1, 1);
	final List<Bike> activeBikes = this.bikeRepository.findActive(GregorianCalendar.from(cutOffDate.atStartOfDay(ZoneId.systemDefault())));

	assertThat(activeBikes.size(), is(equalTo(3)));
	assertThat(activeBikes.get(0).getName(), is(equalTo("bike1")));
	assertThat(activeBikes.get(1).getName(), is(equalTo("bike3")));
    }

    @Test
    public void testFindByName() {
	final Bike bike = this.bikeRepository.findByName("bike1");
	assertThat(bike, is(notNullValue()));
    }

    /**
     * This actualy doesn't test the repository but the mapping, but i wanted to
     * double check if i got the bidirectional mapping right this time. A milage
     * is managed by a bike.
     *
     * @throws SQLException
     */
    @Test
    public void testAddMilage() throws SQLException {
	final Bike bike = this.bikeRepository.findByName("testAddMilageBike");

	bike.addMilage(LocalDate.now(), 23)
		.addMilage(LocalDate.now().plusMonths(1).withDayOfMonth(1), 42);
	this.bikeRepository.save(bike);

	try (
		final Connection connection = this.dataSource.getConnection();
		final PreparedStatement statement = connection.prepareStatement("Select count(*) as cnt from milages where bike_id = " + bike.getId());
		final ResultSet resultSet = statement.executeQuery();) {
	    resultSet.next();
	    assertThat(resultSet.getInt(1), is(equalTo(2)));
	}
    }

    @Test
    public void testGetDateOfFirstRecord() {
	final Calendar dateOfFirstRecord = this.bikeRepository.getDateOfFirstRecord();
	assertThat(LocalDate.from(dateOfFirstRecord.toInstant().atZone(ZoneId.systemDefault())), is(equalTo(LocalDate.of(2012, Month.JANUARY, 1))));
    }
}
