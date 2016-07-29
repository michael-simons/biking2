/*
 * Copyright 2014-2016 michael-simons.eu.
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

import ac.simons.biking2.bikes.highcharts.Options;
import java.util.Arrays;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michael J. Simons, 2014-02-11
 */
public class OptionsTest {

    @Test
    public void testBuilder() {
        final Options options = new Options.Builder<>(object -> object)
            .chart()
                .withBorderWidth(1)
                .build()
            .credits()
                .disable()
                .build()
            .title()
                .withText("Michis milage")
                .build()
            .xAxis()
                .withCategories("test1", "test2")
                .build()
            .yAxis()
                .withMin(0)
                .withMax(2109)
                .withTickInterval(100)
                .enableEndOnTick()
                .title()
                    .withText("test")
                    .build()
                .build()
            .tooltip()
                .withHeaderFormat("th")
                .withPointFormat("pf")
                .withFooterFormat("ff")
                .share()
                .useHTML()
                .build()
            .plotOptions()
                .column()
                    .withPointPadding(0.2)
                    .withBorderWidth(0)
                    .build()
                .series()
                    .disableAnimation()
                    .build()
                .build()
        .build();

        assertThat(options.getChart().getBorderWidth(), is(equalTo(1)));
        assertThat(options.getCredits().isEnabled(), is(false));
        assertThat(options.getTitle().getText(), is(equalTo("Michis milage")));
        assertThat(options.getxAxis().getCategories(), is(equalTo(Arrays.asList("test1", "test2"))));
        assertThat(options.getyAxis().getMin(), is(equalTo(0)));
        assertThat(options.getyAxis().getMax(), is(equalTo(2109)));
        assertThat(options.getyAxis().getTickInterval(), is(equalTo(100)));
        assertThat(options.getyAxis().isEndOnTick(), is(true));
        assertThat(options.getyAxis().getTitle().getText(), is(equalTo("test")));
        assertThat(options.getTooltip().getHeaderFormat(), is(equalTo("th")));
        assertThat(options.getTooltip().getPointFormat(), is(equalTo("pf")));
        assertThat(options.getTooltip().getFooterFormat(), is(equalTo("ff")));
        assertThat(options.getTooltip().isShared(), is(true));
        assertThat(options.getTooltip().isUseHTML(), is(true));
        assertThat(options.getPlotOptions().getColumn().getPointPadding(), is(equalTo(0.2)));
        assertThat(options.getPlotOptions().getColumn().getBorderWidth(), is(equalTo(0)));
        assertThat(options.getPlotOptions().getSeries().isAnimation(), is(false));
    }
}
