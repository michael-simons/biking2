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
package ac.simons.biking2.summary;

import ac.simons.biking2.bikes.AccumulatedPeriod;
import java.util.Calendar;

/**
 * @author Michael J. Simons, 2014-02-13
 */
class Summary {

    private Calendar dateOfFirstRecord;

    private Double total;
        
    private AccumulatedPeriod worstPeriod;
    
    private AccumulatedPeriod bestPeriod;
    
    private Double average;

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

    public AccumulatedPeriod getWorstPeriod() {
	return worstPeriod;
    }

    public void setWorstPeriod(AccumulatedPeriod worstPeriod) {
	this.worstPeriod = worstPeriod;
    }

    public AccumulatedPeriod getBestPeriod() {
	return bestPeriod;
    }

    public void setBestPeriod(AccumulatedPeriod bestPeriod) {
	this.bestPeriod = bestPeriod;
    }    

    public Double getAverage() {
	return average;
    }

    public void setAverage(Double average) {
	this.average = average;
    }
}
