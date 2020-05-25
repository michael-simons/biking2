/*
 * Copyright 2019-2020 michael-simons.eu.
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

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author Michael J. Simons
 * @since 2019-11-02
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class BikeService {

    private final BikeRepository bikeRepository;

    @Transactional
    @CacheEvict(value = "statistics", allEntries = true)
    public BikeEntity createBike(final BikeCmd newBike) {

        final BikeEntity bike = new BikeEntity(newBike.getName(), newBike.boughtOnAsLocalDate());
        bike.setColor(newBike.getColor());
        bike.addMilage(newBike.boughtOnAsLocalDate().withDayOfMonth(1), 0);

        return this.bikeRepository.save(bike);
    }

    @Transactional(readOnly = true)
    public List<BikeEntity> getBikes(final boolean all) {

        List<BikeEntity> rv;
        if (all) {
            rv = bikeRepository.findAll(Sort.by("lastMilage").descending().and(Sort.by("boughtOn", "decommissionedOn", "name").ascending()));
        } else {
            rv = bikeRepository.findByDecommissionedOnIsNull(Sort.by("name").ascending());
        }
        return rv;
    }

    @Transactional
    @CacheEvict(value = "statistics", allEntries = true)
    public MilageEntity createMilage(final Integer id, final NewMilageCmd cmd) {

        final BikeEntity bike = bikeRepository.findById(id).orElseThrow(BikeNotFoundException::new);

        MilageEntity rv;
        if (bike.getDecommissionedOn() != null) {
            throw new BikeAlreadyDecommissionedException();
        } else {
            rv = bike.addMilage(cmd.getRecordedOn(), cmd.getAmount());
            this.bikeRepository.save(bike);
        }

        return rv;
    }

    @Transactional
    @CacheEvict(value = "statistics", allEntries = true)
    public BikeEntity updateBike(final Integer id, final BikeCmd updatedBike) {

        final BikeEntity bike = bikeRepository.findById(id).orElseThrow(BikeNotFoundException::new);

        if (bike.getDecommissionedOn() != null) {
            throw new BikeAlreadyDecommissionedException();
        } else {
            bike.setColor(updatedBike.getColor());
            bike.decommission(updatedBike.decommissionedOnAsLocalDate());
            bike.setMiscellaneous(updatedBike.isMiscellaneous());
        }
        return bike;
    }

    @Transactional
    public BikeEntity updateBikeStory(final Integer id, final StoryCmd newStory) {

        final BikeEntity bike = bikeRepository.findById(id).orElseThrow(BikeNotFoundException::new);

        if (bike.getDecommissionedOn() != null) {
            throw new BikeAlreadyDecommissionedException();
        } else {
            bike.setStory(Optional.ofNullable(newStory).map(c -> new BikeEntity.Link(c.getUrl(), c.getLabel())).orElse(null));
        }
        return bike;
    }

    static class BikeNotFoundException extends RuntimeException {
    }

    static class BikeAlreadyDecommissionedException extends RuntimeException {
    }
}
