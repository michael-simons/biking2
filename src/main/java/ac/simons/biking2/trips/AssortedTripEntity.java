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
package ac.simons.biking2.trips;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "assorted_trips")
@Getter
@EqualsAndHashCode(of = {"coveredOn", "distance"})
public class AssortedTripEntity implements Serializable {

    private static final long serialVersionUID = 3222189732938547117L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "covered_on", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar coveredOn;

    @Column(name = "distance", nullable = false, precision = 8, scale = 2)
    @NotNull
    private BigDecimal distance;

    @SuppressWarnings({"squid:S2637"})
    protected AssortedTripEntity() {
    }

    public AssortedTripEntity(final Calendar coveredOn, final BigDecimal distance) {
        this.coveredOn = coveredOn;
        this.distance = distance;
    }
}
