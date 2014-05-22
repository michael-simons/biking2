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
package ac.simons.biking2.highcharts;

import java.util.Arrays;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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
		.build();
	assertThat(series.getData(), is(equalTo(Arrays.asList(1.0, 2, 3))));
	assertThat(series.getName(), is(equalTo("testName")));
	assertThat(series.getType(), is(equalTo("testType")));
	assertThat(series.getColor(), is(equalTo("AABBCC")));
		
	series = new Series.Builder<>(object -> object)
		.withData(1, 2, 3)
		.withName("testName")
		.withType("testType")
		.build();
	assertThat(series.getData(), is(equalTo(Arrays.asList(1, 2, 3))));
	assertThat(series.getName(), is(equalTo("testName")));
	assertThat(series.getType(), is(equalTo("testType")));
	assertThat(series.getColor(), is(nullValue()));
    }
}
