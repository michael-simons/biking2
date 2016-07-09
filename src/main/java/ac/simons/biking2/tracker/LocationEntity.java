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
package ac.simons.biking2.tracker;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "locations")
public class LocationEntity implements Serializable {

    private static final long serialVersionUID = -9075950345524958606L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "latitude", precision = 18, scale = 15, nullable = false)
    @NotNull
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 18, scale = 15, nullable = false)
    @NotNull
    private BigDecimal longitude;

    @Column(name = "description", length = 2048)
    private String description;

    @Column(name = "created_at", nullable = false, unique = true)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Calendar createdAt;

    protected LocationEntity() {
    }

    public LocationEntity(BigDecimal latitude, BigDecimal longitude, Calendar createdAt) {
	this.latitude = latitude;
	this.longitude = longitude;
	this.createdAt = createdAt;
    }

    public Integer getId() {
	return this.id;
    }

    public Calendar getCreatedAt() {
	return this.createdAt;
    }

    public BigDecimal getLatitude() {
	return this.latitude;
    }

    public BigDecimal getLongitude() {
	return this.longitude;
    }

    public String getDescription() {
	return this.description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 79 * hash + Objects.hashCode(this.latitude);
	hash = 79 * hash + Objects.hashCode(this.longitude);
	hash = 79 * hash + Objects.hashCode(this.createdAt);
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
	final LocationEntity other = (LocationEntity) obj;
	if (!Objects.equals(this.latitude, other.latitude)) {
	    return false;
	}
	if (!Objects.equals(this.longitude, other.longitude)) {
	    return false;
	}
	return Objects.equals(this.createdAt, other.createdAt);
    }
}
