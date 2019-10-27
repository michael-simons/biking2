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
package ac.simons.biking2.bikes.highcharts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 *
 * @since 2014-02-11
 */
class PlotOptionsTest {

    @Test
    void testBuilder() {

        PlotOptions plotOptions = new PlotOptions.Builder<>(object -> object)
                .column()
                    .withPointPadding(0.2)
                    .withBorderWidth(0)
                .build()
                .series()
                    .disableAnimation()
                .build()
        .build();
        assertThat(plotOptions.getColumn().getPointPadding()).isEqualTo(0.2);
        assertThat(plotOptions.getColumn().getBorderWidth()).isEqualTo(0);
        assertThat(plotOptions.getSeries().isAnimation()).isFalse();
    }

}
