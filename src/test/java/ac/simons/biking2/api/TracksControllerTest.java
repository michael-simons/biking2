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

import ac.simons.biking2.persistence.entities.Track;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import ac.simons.biking2.persistence.repositories.TrackRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javax.swing.text.Document;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.springframework.data.domain.Sort;

/**
 * @author Michael J. Simons, 2014-02-15
 */
public class TracksControllerTest {

    private final List<Track> defaultTestData;

    public TracksControllerTest() {
	final int[] ids = new int[]{1, 2, 3};
	final LocalDate now = LocalDate.now();
	final Random random = new Random(System.currentTimeMillis());

	this.defaultTestData = Stream.of(1, 2, 3).map(i -> {
	    final Track t = mock(Track.class);
	    stub(t.getId()).toReturn(i);
	    stub(t.getCoveredOn()).toReturn(GregorianCalendar.from(now.minusDays(random.nextInt(365)).atStartOfDay(ZoneId.systemDefault())));
	    return t;
	}).collect(toList());
    }
     
    @Test
    public void testGetTracks() {
	final TrackRepository trackRepository = mock(TrackRepository.class);
	stub(trackRepository.findAll(new Sort(Sort.Direction.ASC, "coveredOn"))).toReturn(defaultTestData);
	final TracksController tracksController = new TracksController(trackRepository);

	final List<Track> tracks = tracksController.getTracks();

	assertThat(tracks, is(equalTo(defaultTestData)));
    }
}
