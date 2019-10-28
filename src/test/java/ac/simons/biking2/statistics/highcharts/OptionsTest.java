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
 * @author Michael J. Simons
 * 
 * @since 2014-02-11
 */
class OptionsTest {

    @Test
    void testBuilder() {
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

        assertThat(options.getChart().getBorderWidth()).isEqualTo(1);
        assertThat(options.getCredits().isEnabled()).isFalse();
        assertThat(options.getTitle().getText()).isEqualTo("Michis milage");
        assertThat(options.getxAxis().getCategories()).isEqualTo(Arrays.asList("test1", "test2"));
        assertThat(options.getyAxis().getMin()).isEqualTo(0);
        assertThat(options.getyAxis().getMax()).isEqualTo(2109);
        assertThat(options.getyAxis().getTickInterval()).isEqualTo(100);
        assertThat(options.getyAxis().isEndOnTick()).isTrue();
        assertThat(options.getyAxis().getTitle().getText()).isEqualTo("test");
        assertThat(options.getTooltip().getHeaderFormat()).isEqualTo("th");
        assertThat(options.getTooltip().getPointFormat()).isEqualTo("pf");
        assertThat(options.getTooltip().getFooterFormat()).isEqualTo("ff");
        assertThat(options.getTooltip().isShared()).isTrue();
        assertThat(options.getTooltip().isUseHTML()).isTrue();
        assertThat(options.getPlotOptions().getColumn().getPointPadding()).isEqualTo(0.2);
        assertThat(options.getPlotOptions().getColumn().getBorderWidth()).isEqualTo(0);
        assertThat(options.getPlotOptions().getSeries().isAnimation()).isFalse();
    }
}
