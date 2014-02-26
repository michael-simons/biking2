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

import ac.simons.biking2.misc.Summary;
import ac.simons.biking2.persistence.repositories.AssortedTripRepository;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import java.math.BigDecimal;
import java.util.Calendar;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

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

	final MiscApiController controller = new MiscApiController(bikeRepository, this.assortedTripRepository, this.home);

	final Summary summary = controller.getSummary();

	assertThat(summary.getDateOfFirstRecord(), is(equalTo(now)));
	assertThat(summary.getTotal(), is(equalTo(345.0)));
    }
}
