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
package ac.simons.biking2.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Michael J. Simons, 2014-02-20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BikeCmd {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private Date boughtOn;

    @NotBlank
    @Size(max = 6)
    private String color;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Date getBoughtOn() {
	return boughtOn;
    }

    public void setBoughtOn(Date boughtOn) {
	this.boughtOn = boughtOn;
    }

    public String getColor() {
	return color;
    }

    public void setColor(String color) {
	this.color = color;
    }
    
     public LocalDate boughtOnAsLocalDate() {
	return LocalDateTime.ofInstant(this.getBoughtOn().toInstant(), ZoneId.systemDefault()).toLocalDate();
    }
}
