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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 * 
 * @since 2014-08-26
 */
class PlotLineTest {

    @Test
    void testBuilder() {
        PlotLine plotline = new PlotLine.Builder<>(object -> object)
                .at(23.0)
                .withWidth(42.0)
                .withColor("#FFFFFF")
                .build();

        assertThat(plotline.getValue()).isEqualTo(23.0);
        assertThat(plotline.getWidth()).isEqualTo(42.0);
        assertThat(plotline.getColor()).isEqualTo("#FFFFFF");

        plotline = new PlotLine.Builder<>(object -> object)
                .build();

        assertThat(plotline.getValue()).isNull();
        assertThat(plotline.getWidth()).isNull();
        assertThat(plotline.getColor()).isNull();
    }
}
