/*
 * Copyright 2014 Michael J. Simons.
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
package ac.simons.biking2.persistence.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import static java.time.Instant.now;
import java.util.Date;
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
public class Milage implements Serializable {

    private static final long serialVersionUID = 3561438569324691479L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "recorded_on", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date recordedOn;

    @Column(name = "amount", nullable = false, precision = 8, scale = 2)
    @NotNull
    private BigDecimal amount;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "bike_id", referencedColumnName = "id")
    private Bike bike;
    
    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date createdAt;

    @PrePersist
    public void prePersist() {
	if (this.createdAt == null) {
	    this.createdAt = Date.from(now());
	}
    }

    protected Milage() {
    }

    public Milage(final Bike bike) {
	this.bike = bike;
    }

    public Integer getId() {
	return id;
    }

    public Date getRecordedAt() {
	return recordedOn;
    }

    public void setRecordedAt(Date recordedAt) {
	this.recordedOn = recordedAt;
    }

    public BigDecimal getAmount() {
	return amount;
    }

    public void setAmount(BigDecimal amount) {
	this.amount = amount;
    }

    public Date getCreatedAt() {
	return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
	this.createdAt = createdAt;
    }

    public Bike getBike() {
	return bike;
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 61 * hash + Objects.hashCode(this.id);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final Milage other = (Milage) obj;
	if (!Objects.equals(this.id, other.id)) {
	    return false;
	}
	return true;
    }
}
