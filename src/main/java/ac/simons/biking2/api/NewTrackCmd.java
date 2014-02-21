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

import ac.simons.biking2.persistence.entities.Track.Type;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Michael J. Simons, 2014-02-21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewTrackCmd {

    @NotBlank
    @Size(max = 512)
    private String name;

    @NotNull
    private Date coveredOn;

    @Size(max = 2048)
    private String description;

    @NotNull
    private Type type = Type.biking;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Date getCoveredOn() {
	return coveredOn;
    }

    public void setCoveredOn(Date coveredOn) {
	this.coveredOn = coveredOn;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public Type getType() {
	return type;
    }

    public void setType(Type type) {
	this.type = type;
    }
}
