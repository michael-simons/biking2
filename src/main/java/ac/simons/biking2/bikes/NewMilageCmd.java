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
package ac.simons.biking2.bikes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.validation.constraints.NotNull;

/**
 * @author Michael J. Simons, 2014-02-19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class NewMilageCmd {
    @NotNull
    private Date recordedOn;
    @NotNull
    private Double amount;

    public Date getRecordedOn() {
	return recordedOn;
    }

    public void setRecordedOn(Date recordedOn) {
	this.recordedOn = recordedOn;
    }

    public Double getAmount() {
	return amount;
    }

    public void setAmount(Double amount) {
	this.amount = amount;
    }

    public LocalDate recordedOnAsLocalDate() {
	return LocalDateTime.ofInstant(this.getRecordedOn().toInstant(), ZoneId.systemDefault()).toLocalDate();
    }
}
