/*
 * Copyright 2014 msimons.
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
package ac.simons.biking2.bikes.highcharts;

import ac.simons.biking2.bikes.highcharts.Series;
import java.util.Arrays;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Michael J. Simons, 2014-02-12
 */
public class SeriesTest {

    @Test
    public void testBuilder() {
        Series series = new Series.Builder<>(object -> object)
                .withData(1.0, 2, 3)
                .withName("testName")
                .withType("testType")
                .withColor("AABBCC")
                .withLineWidth(1.2)
                .withFillOpacity(0.666)
                .withZIndex(1)
                .linkTo("fump")
                .marker()
                    .build()
                .build();
        assertThat(series.getData(), is(equalTo(Arrays.asList(1.0, 2, 3))));
        assertThat(series.getName(), is(equalTo("testName")));
        assertThat(series.getType(), is(equalTo("testType")));
        assertThat(series.getColor(), is(equalTo("AABBCC")));
        assertThat(series.getLineWidth(), is(equalTo(1.2)));
        assertThat(series.getFillOpacity(), is(equalTo(0.666)));
        assertThat(series.getzIndex(), is(equalTo(1)));
        assertThat(series.getLinkedTo(), is(equalTo("fump")));
        assertThat(series.getMarker(), is(not(nullValue())));

        series = new Series.Builder<>(object -> object)
                .withData(1.0, 2, 3)
                .withName("testName")
                .withType("testType")
                .build();
        assertThat(series.getData(), is(equalTo(Arrays.asList(1.0, 2, 3))));
        assertThat(series.getName(), is(equalTo("testName")));
        assertThat(series.getType(), is(equalTo("testType")));
        assertThat(series.getColor(), is(nullValue()));
        assertThat(series.getLineWidth(), is(nullValue()));
        assertThat(series.getFillOpacity(), is(nullValue()));
        assertThat(series.getzIndex(), is(nullValue()));
        assertThat(series.getLinkedTo(), is(nullValue()));
        assertThat(series.getMarker(), is(nullValue()));
    }
}
