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
package ac.simons.biking2.gallerypictures;

import ac.simons.biking2.support.BeanTester;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 * 
 * @since 2014-05-23
 */
class GalleryPictureEntityTest {

    @Test
    public void beanShouldWorkAsExpected() {

        final LocalDate now = LocalDate.now();

        final Map<String, Object> values = Map.of("description", "description");

        values.forEach(new BeanTester(GalleryPictureEntity.class));

        final GalleryPictureEntity bean = new GalleryPictureEntity(now, "poef");
        final GalleryPictureEntity otherBean = new GalleryPictureEntity(now, "poef");
        Assertions.assertEquals(bean, otherBean);
        Assertions.assertEquals(bean.hashCode(), otherBean.hashCode());
        Assertions.assertNotEquals(bean, "something else");
        Assertions.assertNotEquals(bean, null);
        Assertions.assertEquals(now, bean.getTakenOn());
        Assertions.assertNotNull(bean.getCreatedAt());
        Assertions.assertEquals("poef", bean.getFilename());
        Assertions.assertNull(bean.getId());

        final LocalDate then = now.plusYears(1);
        GalleryPictureEntity other = new GalleryPictureEntity(now, "something else");
        Assertions.assertNotEquals(bean, other);
        other = new GalleryPictureEntity(then, "poef");
        Assertions.assertNotEquals(bean, other);
    }
}
