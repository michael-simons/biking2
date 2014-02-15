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
import ac.simons.biking2.persistence.repositories.TrackRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael J. Simons, 2014-02-15
 */
@RestController
@RequestMapping("/api")
public class TracksController {
    
    private final TrackRepository trackRepository;

    @Autowired
    public TracksController(TrackRepository trackRepository) {
	this.trackRepository = trackRepository;
    }
    
    @RequestMapping("/tracks")
    public List<Track> getTracks() {	
	return trackRepository.findAll(new Sort(Sort.Direction.ASC, "coveredOn"));
    }
}
