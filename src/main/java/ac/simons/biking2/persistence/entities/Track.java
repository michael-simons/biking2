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

import ac.simons.biking2.config.PersistenceConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    @UniqueConstraint(columnNames = {"covered_on", "name"})
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Track implements Serializable {

    private static final long serialVersionUID = 7630613853916630933L;

    /**
     * Converts a string to the real numeric id
     * @param fromPrettyId
     * @return 
     */
    public static Integer getId(final String fromPrettyId) {
	Integer rv = null;
	try {
	    rv = Integer.parseInt(fromPrettyId, 36);
	} catch (NullPointerException | NumberFormatException e) {
	}
	return rv;
    }

    public static enum Type {

	biking, running
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(name = "name", length = 512, nullable = false)
    @NotBlank
    @Size(max = 512)
    private String name;

    @Column(name = "covered_on", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar coveredOn;

    @Column(name = "description", length = 2048)
    private String description;

    @Column(name = "minlat", precision = 18, scale = 15)
    private BigDecimal minlat;

    @Column(name = "minlon", precision = 18, scale = 15)
    private BigDecimal minlon;

    @Column(name = "maxlat", precision = 18, scale = 15)
    private BigDecimal maxlat;

    @Column(name = "maxlon", precision = 18, scale = 15)
    private BigDecimal maxlon;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type = Type.biking;

    public Integer getId() {
	return this.id;
    }

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Calendar getCoveredOn() {
	return this.coveredOn;
    }

    public void setCoveredOn(Calendar coveredOn) {
	this.coveredOn = coveredOn;
    }

    public String getDescription() {
	return this.description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public BigDecimal getMinlat() {
	return this.minlat;
    }

    public void setMinlat(BigDecimal minlat) {
	this.minlat = minlat;
    }

    public BigDecimal getMinlon() {
	return this.minlon;
    }

    public void setMinlon(BigDecimal minlon) {
	this.minlon = minlon;
    }

    public BigDecimal getMaxlat() {
	return this.maxlat;
    }

    public void setMaxlat(BigDecimal maxlat) {
	this.maxlat = maxlat;
    }

    public BigDecimal getMaxlon() {
	return this.maxlon;
    }

    public void setMaxlon(BigDecimal maxlon) {
	this.maxlon = maxlon;
    }

    public Type getType() {
	return type;
    }

    public void setType(Type type) {
	this.type = type;
    }

    @JsonProperty("id")
    public String getPrettyId() {
	return Integer.toString(this.id, 36);
    }

    public File getTrackFile(final File datastoreBaseDirectory, final String format) {
	return new File(datastoreBaseDirectory, String.format("%s/%d.%s", PersistenceConfig.TRACK_DIRECTORY, this.getId(), format));
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
	return Objects.equals(this.id, other.id);
    }
}
