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
package ac.simons.biking2.misc;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Michael J. Simons, 2014-02-13
 */
public class Summary {

    private Calendar dateOfFirstRecord;

    private Double total;
    
    private Map.Entry<LocalDate, Integer> minimumPeriod;
    
    private Map.Entry<LocalDate, Integer> maximumPeriod;

    public Calendar getDateOfFirstRecord() {
	return dateOfFirstRecord;
    }

    public void setDateOfFirstRecord(Calendar dateOfFirstRecord) {
	this.dateOfFirstRecord = dateOfFirstRecord;
    }

    public Double getTotal() {
	return total;
    }

    public void setTotal(Double total) {
	this.total = total;
    }

    public Entry<LocalDate, Integer> getMinimumPeriod() {
	return minimumPeriod;
    }

    public void setMinimumPeriod(Entry<LocalDate, Integer> minimumPeriod) {
	this.minimumPeriod = minimumPeriod;
    }

    public Entry<LocalDate, Integer> getMaximumPeriod() {
	return maximumPeriod;
    }

    public void setMaximumPeriod(Entry<LocalDate, Integer> maximumPeriod) {
	this.maximumPeriod = maximumPeriod;
    }
}
