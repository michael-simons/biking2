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
package ac.simons.biking2.statistics.highcharts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Michael J. Simons
 * 
 * @since 2014-02-12
 */
class SeriesTest {

    @Test
    void testBuilder() {
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
        assertThat(series.getData()).isEqualTo(Arrays.asList(1.0, 2, 3));
        assertThat(series.getName()).isEqualTo("testName");
        assertThat(series.getType()).isEqualTo("testType");
        assertThat(series.getColor()).isEqualTo("AABBCC");
        assertThat(series.getLineWidth()).isEqualTo(1.2);
        assertThat(series.getFillOpacity()).isEqualTo(0.666);
        assertThat(series.getzIndex()).isEqualTo(1);
        assertThat(series.getLinkedTo()).isEqualTo("fump");
        assertThat(series.getMarker()).isNotNull();

        series = new Series.Builder<>(object -> object)
                .withData(1.0, 2, 3)
                .withName("testName")
                .withType("testType")
                .build();
        assertThat(series.getData()).isEqualTo(Arrays.asList(1.0, 2, 3));
        assertThat(series.getName()).isEqualTo("testName");
        assertThat(series.getType()).isEqualTo("testType");
        assertThat(series.getColor()).isNull();
        assertThat(series.getLineWidth()).isNull();
        assertThat(series.getFillOpacity()).isNull();
        assertThat(series.getzIndex()).isNull();
        assertThat(series.getLinkedTo()).isNull();
        assertThat(series.getMarker()).isNull();
    }
}
