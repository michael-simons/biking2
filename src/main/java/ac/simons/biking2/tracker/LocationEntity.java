/*
 * Copyright 2014-2016 michael-simons.eu.
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "locations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = {"latitude", "longitude", "createdAt"})
public class LocationEntity implements Serializable {

    private static final long serialVersionUID = -9075950345524958606L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(precision = 18, scale = 15, nullable = false)
    @NotNull
    private BigDecimal latitude;

    @Column(precision = 18, scale = 15, nullable = false)
    @NotNull
    private BigDecimal longitude;

    @Column(length = 2048)
    @Setter
    private String description;

    @Column(name = "created_at", nullable = false, unique = true)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Calendar createdAt;

    LocationEntity(final BigDecimal latitude, final BigDecimal longitude, final Calendar createdAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
    }
}
