/*
 * Copyright 2014-2023 michael-simons.eu.
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

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author Michael J. Simons
 * @since 2014-02-20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record BikeCmd(

    @NotBlank
    @Size(max = 255)
    String name,

    @DateTimeFormat(iso = DATE_TIME)
    @NotNull
    ZonedDateTime boughtOn,

    @NotBlank
    @Size(max = 6)
    String color,

    @DateTimeFormat(iso = DATE_TIME)
    ZonedDateTime decommissionedOn,

    boolean miscellaneous
) {

    public LocalDate boughtOnAsLocalDate() {
        return this.boughtOn.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
    }

    public LocalDate decommissionedOnAsLocalDate() {
        return this.decommissionedOn == null ? null : this.decommissionedOn.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
    }

}
