/*
 * Copyright 2015-2023 michael-simons.eu.
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
package ac.simons.biking2.trips;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

/**
 * Command class for creating new assorted trips.
 *
 * @author Michael J. Simons
 * @since 2015-06-09
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NewTripCmd(

    @NotNull
    LocalDate coveredOn,
    @NotNull
    Double distance
) {
}
