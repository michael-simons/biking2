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
package ac.simons.biking2.tracker;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author Michael J. Simons, 2014-03-20
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocationService {

    private final LocationRepository locationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public LocationEntity createAndSendNewLocation(final NewLocationCmd newLocation) {
        final LocationEntity location = this.locationRepository.save(new LocationEntity(newLocation.getLatitude(), newLocation.getLongitude(), newLocation.getCreatedAt()));
        this.messagingTemplate.convertAndSend("/topic/currentLocation", location);
        return location;
    }

    public List<LocationEntity> getLocationsForTheLastNHours(final int hours) {
        return locationRepository.findByCreatedAtGreaterThanOrderByCreatedAtAsc(OffsetDateTime.now().minusHours(hours));
    }

    /**
     * @return The total number of locations tracked
     */
    public long getLocationCount() {
        return locationRepository.count();
    }
}
