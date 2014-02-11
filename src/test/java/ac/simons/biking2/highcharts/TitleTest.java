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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-02-11
 */
public class TitleTest {

    @Test
    public void testBuilder() {
	new Title.Builder<>(title -> {
	    assertThat(title.getText(), is(nullValue()));
	    return TitleTest.this;
	})
	.build();

	new Title.Builder<>(title -> {
	    assertThat(title.getText(), is(equalTo("test 123")));
	    return TitleTest.this;
	})
		.withText("test 123")
	.build();
    }
}
