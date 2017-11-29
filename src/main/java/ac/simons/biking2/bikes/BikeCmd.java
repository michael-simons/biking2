/*
 * Copyright 2014-2017 michael-simons.eu.
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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Michael J. Simons, 2014-02-20
 */
@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class BikeCmd {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private Date boughtOn;

    @NotBlank
    @Size(max = 6)
    private String color;

    private Date decommissionedOn;

    public LocalDate boughtOnAsLocalDate() {
        return LocalDateTime.ofInstant(this.boughtOn.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }

    public LocalDate decommissionedOnAsLocalDate() {
        return this.decommissionedOn == null ? null : LocalDateTime.ofInstant(this.decommissionedOn.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }
}
