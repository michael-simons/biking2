/*
 * Copyright 2014 Michael J. Simons.
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
package ac.simons.biking2.highcharts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael J. Simons
 */
public class HighchartDefinitionTest {

    @Test
    public void testBuilder() {
	HighchartDefinition.Builder builder = HighchartDefinition.define();
	
	assertThat(builder.computeCurrentMaxYValue(), is(equalTo(0)));
	
	builder
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
	    .series()
		.withData(1, 2)
		.build()
	    .series()
		.withData(3, 4)
		.build();
	
	assertThat(builder.computeCurrentMaxYValue(), is(equalTo(4)));
	
	final HighchartDefinition definition = builder.build();		
		
	assertThat(definition.getChart().getBorderWidth(), is(equalTo(1)));
	assertThat(definition.getCredits().isEnabled(), is(false));
	assertThat(definition.getTitle().getText(), is(equalTo("Michis milage")));
	assertThat(definition.getxAxis().getCategories(), is(equalTo(Arrays.asList("test1", "test2"))));
	assertThat(definition.getyAxis().getMin(), is(equalTo(0)));
	assertThat(definition.getyAxis().getMax(), is(equalTo(2109)));
	assertThat(definition.getyAxis().getTickInterval(), is(equalTo(100)));
	assertThat(definition.getyAxis().isEndOnTick(), is(true));
	assertThat(definition.getyAxis().getTitle().getText(), is(equalTo("test")));
	assertThat(definition.getTooltip().getHeaderFormat(), is(equalTo("th")));
	assertThat(definition.getTooltip().getPointFormat(), is(equalTo("pf")));
	assertThat(definition.getTooltip().getFooterFormat(), is(equalTo("ff")));
	assertThat(definition.getTooltip().isShared(), is(true));
	assertThat(definition.getTooltip().isUseHTML(), is(true));
	assertThat(definition.getPlotOptions().getColumn().getPointPadding(), is(equalTo(0.2)));
	assertThat(definition.getPlotOptions().getColumn().getBorderWidth(), is(equalTo(0)));
	assertThat(definition.getPlotOptions().getSeries().isAnimation(), is(false));
	assertThat(definition.getSeries().size(), is(equalTo(2)));
	final List<Series> hlp = new ArrayList<>(definition.getSeries());
	assertThat(hlp.get(0).getData(), is(equalTo(Arrays.asList(1, 2))));
	assertThat(hlp.get(1).getData(), is(equalTo(Arrays.asList(3, 4))));	
    }
}
