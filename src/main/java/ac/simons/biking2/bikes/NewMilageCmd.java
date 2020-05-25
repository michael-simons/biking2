/*
 * Copyright 2014-2020 michael-simons.eu.
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Michael J. Simons
 * @since 2014-02-19
 */
@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class NewMilageCmd {

    @DateTimeFormat(iso = DATE_TIME)
    @NotNull
    private LocalDate recordedOn;

    @NotNull
    @Positive
    private Double amount;
}
