/*
 * Copyright 2019 michael-simons.eu.
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
package ac.simons.biking2.statistics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jooq.lambda.tuple.Tuple2;

/**
 * Monthly statistics for a single year.
 *
 * @author Michael J. Simons
 * @since 2019-10-31
 */
final class MonthlyStatistics {

    private final int[] totals;

    /**
     * Key is bike and color.
     */
    private final Map<Tuple2<String, String>, int[]> values;

    MonthlyStatistics(final int[] totals, final Map<Tuple2<String, String>, int[]> values) {
        this.totals = totals;
        this.values = new LinkedHashMap<>(values);
    }

    public int[] getTotals() {
        return totals;
    }

    public Map<Tuple2<String, String>, int[]> getValues() {
        return Collections.unmodifiableMap(values);
    }
}
