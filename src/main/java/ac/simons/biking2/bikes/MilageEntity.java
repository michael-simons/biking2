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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Michael J. Simons
 * @since 2014-02-08
 */
@Entity
@Table(name = "milages", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"bike_id", "recorded_on"})
})
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("JpaObjectClassSignatureInspection")
@Getter
@EqualsAndHashCode(of = {"bike", "recordedOn"})
public class MilageEntity implements Serializable, Comparable<MilageEntity> {

    private static final long serialVersionUID = 3561438569324691479L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "recorded_on", nullable = false)
    @NotNull
    private LocalDate recordedOn;

    @Column(nullable = false, precision = 8, scale = 2)
    @NotNull
    private BigDecimal amount;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "bike_id", referencedColumnName = "id")
    private BikeEntity bike;

    @Column(name = "created_at", nullable = false)
    @NotNull
    private OffsetDateTime createdAt;

    protected MilageEntity(final BikeEntity bike, final LocalDate recordedOn, final double amount) {
        this.bike = bike;
        this.recordedOn = recordedOn;
        this.amount = BigDecimal.valueOf(amount);
        this.createdAt = OffsetDateTime.now();
    }

    @Override
    public int compareTo(final MilageEntity o) {
        return this.recordedOn.compareTo(o.recordedOn);
    }
}
