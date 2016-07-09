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
package ac.simons.biking2.bikes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "milages", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"bike_id", "recorded_on"})
})
public class MilageEntity implements Serializable, Comparable<MilageEntity> {

    private static final long serialVersionUID = 3561438569324691479L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "recorded_on", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar recordedOn;

    @Column(name = "amount", nullable = false, precision = 8, scale = 2)
    @NotNull
    private BigDecimal amount;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "bike_id", referencedColumnName = "id")
    private BikeEntity bike;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Calendar createdAt;

    protected MilageEntity() {
    }

    protected MilageEntity(final BikeEntity bike, final LocalDate recordedOn, final double amount) {
        this.bike = bike;
        this.recordedOn = GregorianCalendar.from(recordedOn.atStartOfDay(ZoneId.systemDefault()));
        this.amount = BigDecimal.valueOf(amount);
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Calendar.getInstance();
        }
    }

    public Integer getId() {
        return id;
    }

    public Calendar getRecordedOn() {
        return recordedOn;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public BikeEntity getBike() {
        return bike;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.recordedOn);
        hash = 31 * hash + Objects.hashCode(this.bike);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MilageEntity other = (MilageEntity) obj;
        if (!Objects.equals(this.recordedOn, other.recordedOn)) {
            return false;
        }
        if (!Objects.equals(this.amount, other.amount)) {
            return false;
        }
        return Objects.equals(this.bike, other.bike);
    }

    @Override
    public int compareTo(final MilageEntity o) {
        return this.recordedOn.compareTo(o.recordedOn);
    }
}
