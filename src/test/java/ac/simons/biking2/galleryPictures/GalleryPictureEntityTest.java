/*
 * Copyright 2014 michael-simons.eu.
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
package ac.simons.biking2.galleryPictures;

import ac.simons.biking2.support.BeanTester;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class GalleryPictureEntityTest {

    @Test
    public void beanShouldWorkAsExpected() {
        final Calendar now = Calendar.getInstance();

        final Map<String, Object> values = new HashMap<>();
        values.put("description", "description");

        values.forEach(new BeanTester(GalleryPictureEntity.class));

        final GalleryPictureEntity bean = new GalleryPictureEntity(now, "poef");
        final GalleryPictureEntity otherBean = new GalleryPictureEntity(now, "poef");
        Assert.assertEquals(bean, otherBean);
        Assert.assertEquals(bean.hashCode(), otherBean.hashCode());
        Assert.assertNotEquals(bean, "something else");
        Assert.assertNotEquals(bean, null);
        Assert.assertEquals(now, bean.getTakenOn());
        Assert.assertNotNull(bean.getCreatedAt());
        Assert.assertEquals("poef", bean.getFilename());
        Assert.assertNull(bean.getId());

        final Calendar then = Calendar.getInstance();
        then.add(Calendar.YEAR, 1);
        GalleryPictureEntity other = new GalleryPictureEntity(now, "something else");
        Assert.assertNotEquals(bean, other);
        other = new GalleryPictureEntity(then, "poef");
        Assert.assertNotEquals(bean, other);
    }
}
