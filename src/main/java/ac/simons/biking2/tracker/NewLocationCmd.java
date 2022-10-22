/*
 * Copyright 2014-2021 michael-simons.eu.
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
package ac.simons.biking2.tracker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import jakarta.validation.constraints.NotNull;

/**
 * @author Michael J. Simons,
 * @since 2014-03-20
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record NewLocationCmd(

    @JsonProperty(value = "lat")
    @NotNull
    BigDecimal latitude,

    @JsonProperty(value = "lon")
    @NotNull
    BigDecimal longitude,

    @JsonProperty(value = "tst")
    Long timestampSeconds,

    @JsonProperty(value = "tstMillis")
    Long timestampMillis
) {

    public OffsetDateTime createdAt() {

        Instant createdAt;
        if (this.timestampSeconds != null) {
            createdAt = Instant.ofEpochSecond(this.timestampSeconds);
        } else if (this.timestampMillis != null) {
            createdAt = Instant.ofEpochMilli(this.timestampMillis);
        } else {
            throw new IllegalStateException("Either timestampSeconds or timestampMillis must be set.");
        }
        return OffsetDateTime.ofInstant(createdAt, ZoneId.systemDefault());
    }
}
