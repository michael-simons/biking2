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
package ac.simons.biking2.bikingpictures;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Michael J. Simons
 *
 * @since 2014-02-18
 */
@DataJpaTest
@ActiveProfiles("test")
class BikingPictureRepositoryTest {

    @Autowired
    private BikingPictureRepository bikingPictureRepository;

    @Test
    @Rollback(true)
    void getMaxPubDate_shouldWork() {
        OffsetDateTime value = bikingPictureRepository.getMaxPubDate();
        OffsetDateTime expected = ZonedDateTime.of(2003, 9, 21, 14, 13, 0, 0, ZoneId.systemDefault()).toOffsetDateTime();

        assertEquals(expected, value);

        bikingPictureRepository.deleteAll();

        value = bikingPictureRepository.getMaxPubDate();
        expected = ZonedDateTime.of(2005, 8, 7, 18, 30, 42, 00, ZoneId.systemDefault()).toOffsetDateTime();
        assertEquals(expected, value);
    }
}
