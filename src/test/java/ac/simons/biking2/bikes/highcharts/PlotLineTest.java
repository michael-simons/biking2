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
package ac.simons.biking2.bikes.highcharts;

import ac.simons.biking2.bikes.highcharts.PlotLine;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michael J. Simons, 2014-08-26
 */
public class PlotLineTest {

    @Test
    public void testBuilder() {
        PlotLine plotline = new PlotLine.Builder<>(object -> object)
                .at(23.0)
                .withWidth(42.0)
                .withColor("#FFFFFF")
                .build();

        assertThat(plotline.getValue(), is(equalTo(23.0)));
        assertThat(plotline.getWidth(), is(equalTo(42.0)));
        assertThat(plotline.getColor(), is(equalTo("#FFFFFF")));

        plotline = new PlotLine.Builder<>(object -> object)
                .build();

        assertThat(plotline.getValue(), is(nullValue()));
        assertThat(plotline.getWidth(), is(nullValue()));
        assertThat(plotline.getColor(), is(nullValue()));
    }
}
