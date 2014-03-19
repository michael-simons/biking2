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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "locations")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location implements Serializable {

    private static final long serialVersionUID = -9075950345524958606L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonProperty(value = "lat")
    @Column(name = "latitude", precision = 18, scale = 15, nullable = false)
    @NotNull
    private BigDecimal latitude;

    @JsonProperty(value = "lon")
    @Column(name = "longitude", precision = 18, scale = 15, nullable = false)
    @NotNull
    private BigDecimal longitude;

    @Column(name = "description", length = 2048)
    private String description;

    @JsonProperty(value = "tst")
    @Column(name = "created_at", nullable = false, unique = true)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Calendar createdAt;

    @PrePersist
    public void prePersist() {
	if (this.createdAt == null) {
	    this.createdAt = Calendar.getInstance();
	}
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

    public void setLatitude(BigDecimal latitude) {
	this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
	return this.longitude;
    }

    public void setLongitude(BigDecimal longitude) {
	this.longitude = longitude;
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
	hash = 41 * hash + Objects.hashCode(this.id);
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
	final Location other = (Location) obj;
	return Objects.equals(this.id, other.id);
    }
}
