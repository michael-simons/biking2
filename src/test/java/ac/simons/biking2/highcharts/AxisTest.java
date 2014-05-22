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

import java.util.Arrays;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Michael J. Simons
 */
public class AxisTest {

    @Test
    public void testBuilder() {
	Axis axis = new Axis.Builder<>(object -> object)
		.withCategories("a", "b")
		.withMin(0)
		.withMax(2109)
		.withTickInterval(100)
		.enableEndOnTick()
		.title()
		    .withText("test")
		.build()
	.build();
	assertThat(axis.getCategories(), is(equalTo(Arrays.asList("a", "b"))));
	assertThat(axis.getMin(), is(equalTo(0)));
	assertThat(axis.getMax(), is(equalTo(2109)));
	assertThat(axis.getTickInterval(), is(equalTo(100)));
	assertThat(axis.isEndOnTick(), is(true));
	assertThat(axis.getTitle().getText(), is(equalTo("test")));
	
	axis = new Axis.Builder<>(object -> object)	
		.enableEndOnTick()
		.disableEndOnTick()	
	.build();
	assertThat(axis.isEndOnTick(), is(false));	
    }

}
