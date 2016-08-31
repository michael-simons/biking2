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
package ac.simons.biking2.bikes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import lombok.Getter;

/**
 * Represents a accumulated period value
 *
 * @author Michael J. Simons, 2014-05-05
 */
@Getter
public final class AccumulatedPeriod {
    private final Calendar startOfPeriod;

    private final int value;

    public AccumulatedPeriod(final LocalDate startOfPeriod, final int value) {
        this(GregorianCalendar.from(startOfPeriod.atStartOfDay(ZoneId.systemDefault())), value);
    }

    public AccumulatedPeriod(final Calendar startOfPeriod, final int value) {
        this.startOfPeriod = startOfPeriod;
        this.value = value;
    }
}
