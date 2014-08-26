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
import java.util.Collection;
import java.util.List;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michael J. Simons, 2014-02-11
 */
public class HighchartsNgConfigTest {
    
    @Test
    public void testBuilder() {
	final HighchartsNgConfig.Builder builder = HighchartsNgConfig.define();
	
	assertThat(builder.computeCurrentMaxYValue(), is(equalTo(0)));
	
	final Collection<Series<?>> series = builder
	    .series()
   		.withData(1, 2)
   		.build()
   	    .series()
   		.withData(3, 4)
   		.build()
	.build().getSeries();
	
	assertThat(builder.computeCurrentMaxYValue(), is(equalTo(4)));

	assertThat(series.size(), is(equalTo(2)));
	final List<Series> hlp = new ArrayList<>(series);
	assertThat(hlp.get(0).getData(), is(equalTo(Arrays.asList(1, 2))));
	assertThat(hlp.get(1).getData(), is(equalTo(Arrays.asList(3, 4))));
    }
    
    @Test
    public void testJsonCreator() {
	final HighchartsNgConfig config = new HighchartsNgConfig(null, null);
	// Make sure no user data can be injected
	assertThat(config.getUserData(), is(nullValue()));
    }
}
