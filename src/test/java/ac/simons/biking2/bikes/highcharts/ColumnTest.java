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


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 *
 * @since 2014-02-11
 */
class ColumnTest {

    @Test
    void testBuilder() {
        Column column = new Column.Builder<>(object -> object)
                .withPointPadding(0.2)
                .withBorderWidth(0)
        .build();
        assertThat(column.getPointPadding(), is(equalTo(0.2)));
        assertThat(column.getBorderWidth(), is(equalTo(0)));
    }
}
