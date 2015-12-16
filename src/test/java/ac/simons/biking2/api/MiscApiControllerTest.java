/*
 * Copyright 2014 Michael J. Simons.
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
package ac.simons.biking2.api;

import ac.simons.biking2.bikes.ChartsControllerTest;
import ac.simons.biking2.misc.About;
import ac.simons.biking2.misc.Summary;
import ac.simons.biking2.bikes.BikeEntity;
import ac.simons.biking2.persistence.repositories.AssortedTripRepository;
import ac.simons.biking2.bikes.BikeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

import static java.time.LocalDate.now;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static java.time.LocalDate.now;
import static java.time.LocalDate.now;
import static java.time.LocalDate.now;

/**
 * @author Michael J. Simons, 2014-02-17
 */
public class MiscApiControllerTest {
    private final Coordinate home = new Coordinate("13.408056", "52.518611");
    private final AssortedTripRepository assortedTripRepository;
    
    public MiscApiControllerTest() {
	this.assortedTripRepository = mock(AssortedTripRepository.class);
	stub(this.assortedTripRepository.getTotalDistance()).toReturn(BigDecimal.TEN);
    }
    
    @Test
    public void testGetSummary() {
	final Calendar now = Calendar.getInstance();

	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findAll()).toReturn(ChartsControllerTest.generateTestData());
	stub(bikeRepository.getDateOfFirstRecord()).toReturn(now);

	final MiscApiController controller = new MiscApiController(bikeRepository, this.assortedTripRepository, this.home, null);

	final Summary summary = controller.getSummary();

	assertThat(summary.getDateOfFirstRecord(), is(equalTo(now)));
	assertThat(summary.getTotal(), is(equalTo(345.0)));
    }
    
    @Test
    public void testGetSummaryMinMaxPeriods() {
	final Calendar now = Calendar.getInstance();	
	final List<BikeEntity> bikes = new ArrayList<>();	
	// A bike with no milage should not lead to an error
	bikes.add(new BikeEntity("no-milage", now()));
	bikes.add(new BikeEntity("some-milage", now())
		    .addMilage(LocalDate.of(2009,1,1), 10).getBike()
		    .addMilage(LocalDate.of(2009,2,1), 30).getBike()		
		    .addMilage(LocalDate.of(2009,3,1), 33).getBike()		
	);
	bikes.add(new BikeEntity("more-milage", now())
		    .addMilage(LocalDate.of(2009,1,1),  0).getBike()
		    .addMilage(LocalDate.of(2009,2,1), 30).getBike()		
		    .addMilage(LocalDate.of(2009,3,1), 70).getBike()						    	
	);
	
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findAll()).toReturn(bikes);
	stub(bikeRepository.getDateOfFirstRecord()).toReturn(now);

	final MiscApiController controller = new MiscApiController(bikeRepository, this.assortedTripRepository, this.home, null);
	final Summary summary = controller.getSummary();
	assertNotNull(summary.getWorstPeriod());	
	assertThat(summary.getWorstPeriod().getStartOfPeriod(), is(equalTo(GregorianCalendar.from(LocalDate.of(2009,2,1).atStartOfDay(ZoneId.systemDefault())))));
	assertThat(summary.getWorstPeriod().getValue(), is(equalTo(43)));
	
	assertNotNull(summary.getBestPeriod());	
	assertThat(summary.getBestPeriod().getStartOfPeriod(), is(equalTo(GregorianCalendar.from(LocalDate.of(2009,1,1).atStartOfDay(ZoneId.systemDefault())))));
	assertThat(summary.getBestPeriod().getValue(), is(equalTo(50)));
	
	assertThat(summary.getAverage(), is(equalTo(93.0/2)));
    }
    
    @Test
    public void testGetSummaryMinMaxPeriodsWithoutPeriods() {
	final Calendar now = Calendar.getInstance();	
	final List<BikeEntity> bikes = new ArrayList<>();	
	bikes.add(new BikeEntity("no-milage", now()));
	
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findAll()).toReturn(bikes);
	stub(bikeRepository.getDateOfFirstRecord()).toReturn(now);
	
	final MiscApiController controller = new MiscApiController(bikeRepository, this.assortedTripRepository, this.home, null);
	final Summary summary = controller.getSummary();
	assertNull(summary.getWorstPeriod());	
	assertNull(summary.getBestPeriod());	
	
	assertThat(summary.getAverage(), is(equalTo(0.0)));
    }
    
    @Test
    public void testGetHome() {
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	final MiscApiController controller = new MiscApiController(bikeRepository, this.assortedTripRepository, this.home, null);
	
	Assert.assertEquals(this.home, controller.getHome());
    }
    
    @Test
    public void testGetAbout() {
	final BuildProperties buildProperties = new BuildProperties();
	
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	final MiscApiController controller = new MiscApiController(bikeRepository, this.assortedTripRepository, this.home, buildProperties);
	
	final About about = controller.getAbout();
	Assert.assertNotNull(about);
	Assert.assertNotNull(about.getVm());
	Assert.assertNotNull(about.getBuild());
	Assert.assertEquals(buildProperties, about.getBuild());
    }
}
