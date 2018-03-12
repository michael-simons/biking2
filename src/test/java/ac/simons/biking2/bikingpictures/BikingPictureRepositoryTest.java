/*
 * Copyright 2014-2018 michael-simons.eu.
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

import ac.simons.biking2.support.TestConfig;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static java.time.ZonedDateTime.of;
import static java.time.ZonedDateTime.ofInstant;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author Michael J. Simons, 2014-02-18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
public class BikingPictureRepositoryTest {

    @Autowired
    private BikingPictureRepository bikingPictureRepository;

    @Test
    @Rollback(true)
    public void getMaxPubDate_shouldWork() {
        ZonedDateTime value = ofInstant(bikingPictureRepository.getMaxPubDate().toInstant(), ZoneId.systemDefault());
        ZonedDateTime expected = of(2003, 9, 21, 14, 13, 00, 00, value.getZone());
        Assert.assertThat(value, is(equalTo(expected)));

        bikingPictureRepository.deleteAll();

        value = ofInstant(bikingPictureRepository.getMaxPubDate().toInstant(), ZoneId.systemDefault());
        expected = of(2005, 8, 7, 18, 30, 42, 00, value.getZone());
        Assert.assertThat(value, is(equalTo(expected)));
    }
}
