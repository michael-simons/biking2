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
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "tracks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"when", "name"})
})
public class Track implements Serializable {

    private static final long serialVersionUID = 7630613853916630933L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", unique = true, length = 512, nullable = false)
    @NotBlank
    @Size(max = 512)
    private String name;

    @Column(name = "when", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date when;

    @Column(name = "description")
    private String description;

    @Column(name = "minlat", precision = 18, scale = 15)
    private BigDecimal minlat;

    @Column(name = "minlon", precision = 18, scale = 15)
    private BigDecimal minlon;

    @Column(name = "maxlat", precision = 18, scale = 15)
    private BigDecimal maxlat;

    @Column(name = "maxlon", precision = 18, scale = 15)
    private BigDecimal maxlon;

    public Integer getId() {
	return id;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Date getWhen() {
	return when;
    }

    public void setWhen(Date when) {
	this.when = when;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public BigDecimal getMinlat() {
	return minlat;
    }

    public void setMinlat(BigDecimal minlat) {
	this.minlat = minlat;
    }

    public BigDecimal getMinlon() {
	return minlon;
    }

    public void setMinlon(BigDecimal minlon) {
	this.minlon = minlon;
    }

    public BigDecimal getMaxlat() {
	return maxlat;
    }

    public void setMaxlat(BigDecimal maxlat) {
	this.maxlat = maxlat;
    }

    public BigDecimal getMaxlon() {
	return maxlon;
    }

    public void setMaxlon(BigDecimal maxlon) {
	this.maxlon = maxlon;
    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 67 * hash + Objects.hashCode(this.id);
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
	final Track other = (Track) obj;
	if (!Objects.equals(this.id, other.id)) {
	    return false;
	}
	return true;
    }
}
