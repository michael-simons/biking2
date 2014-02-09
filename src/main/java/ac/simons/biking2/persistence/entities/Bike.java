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
import static java.time.Instant.now;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "bikes")
public class Bike implements Serializable {

    private static final long serialVersionUID = 1249824815158908981L;
    
    public static class BikingPeriod {
	public final LocalDate start;
	public final Integer milage;

	public BikingPeriod(LocalDate start, Integer milage) {
	    this.start = start;
	    this.milage = milage;
	}
	
	public BikingPeriod(Date start, Integer milage) {
	    this(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), milage);
	}

	@Override
	public int hashCode() {
	    int hash = 7;
	    hash = 79 * hash + Objects.hashCode(this.start);
	    hash = 79 * hash + Objects.hashCode(this.milage);
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
	    final BikingPeriod other = (BikingPeriod) obj;
	    if (!Objects.equals(this.start, other.start)) {
		return false;
	    }
	    if (!Objects.equals(this.milage, other.milage)) {
		return false;
	    }
	    return true;
	}
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", unique = true, length = 255, nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "color", length = 6, nullable = false)
    @NotBlank
    @Size(max = 6)
    private String color = "CCCCCC";    
    
    @Column(name = "decommissioned_on")
    @Temporal(TemporalType.DATE)    
    private Date decommissionedOn;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "bike")
    @OrderBy("recordedOn asc")
    private List<Milage> milages = new ArrayList<>();   
    
    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date createdAt;
    
    /** Contains all monthly periods that bike has been used */
    private transient List<BikingPeriod> periods;

    @PrePersist
    public void prePersist() {
	if (this.createdAt == null) {
	    this.createdAt = Date.from(now());
	}
    }

    public Integer getId() {
	return this.id;
    }
    
    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getColor() {
	return this.color;
    }

    public void setColor(String color) {
	this.color = color;
    }

    public Date getDecommissionedOn() {
	return this.decommissionedOn;
    }

    public void setDecommissionedOn(Date decommissionedOn) {
	this.decommissionedOn = decommissionedOn;
    }

    public List<Milage> getMilages() {
	return this.milages;
    }

    public void setMilages(List<Milage> milages) {
	this.milages = milages;
    }

    public Date getCreatedAt() {
	return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
	this.createdAt = createdAt;
    }
    
    public List<BikingPeriod> getPeriods() {
	if(this.periods == null) {	    
	    this.periods = (this.milages == null || milages.size() == 1) ? new ArrayList<>() : range(1, milages.size())
		.mapToObj(i -> {
		    final Milage left = milages.get(i-1);			
		    return new BikingPeriod(left.getRecordedAt(), milages.get(i).getAmount().subtract(left.getAmount()).intValue());
		}).collect(toList());
	}
	return this.periods;
    }
    
    public Integer getMilage() {
	return this.getPeriods().parallelStream().mapToInt(period -> period.milage).sum();
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 17 * hash + Objects.hashCode(this.id);
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
	final Bike other = (Bike) obj;
	if (!Objects.equals(this.id, other.id)) {
	    return false;
	}
	return true;
    }   
}