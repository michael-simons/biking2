/*
 * Copyright 2021 michael-simons.eu.
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a period in which a bike was lent and the amount of milage travelled in that period
 * @author Michael J. Simons
 * @since 2021-04-04
 */
@Entity
@Table(name = "lent_milages", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"bike_id", "lent_on"})
})
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("JpaObjectClassSignatureInspection")
@Getter
@EqualsAndHashCode(of = {"bike", "lentOn"})
public class LentMilageEntity implements Serializable, Comparable<LentMilageEntity> {

    private static final long serialVersionUID = 4531438569324611479L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lent_on", nullable = false)
    @NotNull
    private LocalDate lentOn;

    @Column(name = "returned_on")
    @NotNull
    private LocalDate returnedOn;

    @Column(nullable = false, precision = 8, scale = 2)
    @NotNull
    private BigDecimal amount;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "bike_id", referencedColumnName = "id")
    private BikeEntity bike;

    @Column(name = "created_at", nullable = false)
    @NotNull
    private OffsetDateTime createdAt;

    protected LentMilageEntity(final BikeEntity bike, final LocalDate lentOn) {
        this.bike = bike;
        this.lentOn = lentOn;
        this.createdAt = OffsetDateTime.now();
    }

    public LocalDate getLentOn() {
        return lentOn;
    }

    public LocalDate getReturnedOn() {
        return returnedOn;
    }

    public void setReturnedOn(final LocalDate returnedOn) {
        this.returnedOn = returnedOn;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isLent() {
        return this.returnedOn == null;
    }

    @Override
    public int compareTo(final LentMilageEntity o) {

        int r = this.lentOn.compareTo(o.getLentOn());
        if (r == 0 && this.returnedOn != null && o.getReturnedOn() != null) {
            r = this.returnedOn.compareTo(o.getReturnedOn());
        }
        return r;
    }
}
