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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.reducing;
import java.util.stream.IntStream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@NamedQueries({
    @NamedQuery(
	    name = "Bike.findActive",
	    query
	    = "Select b from Bike b "
	    + " where b.decommissionedOn is null "
	    + "    or b.decommissionedOn >= :cutoffDate "
	    + " order by b.name asc "
    )
})
public class Bike implements Serializable {

    private static final long serialVersionUID = 1249824815158908981L;

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

    /**
     * Contains all monthly periods that bike has been used
     */
    private transient Map<LocalDate, Integer> periods;

    protected Bike() {
    }

    public Bike(String name) {
	this.name = name;
    }

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

    /**
     * An IntStream is used to create indizes, which are collected in a HashMap
     * supplied by the supplier HashMap::new, a method reference capture for the
     * constructor.
     *
     * @return
     */
    public synchronized Map<LocalDate, Integer> getPeriods() {
	if (this.periods == null) {
	    this.periods = IntStream.range(1, this.milages.size()).collect(HashMap::new, (map, i) -> {
		final Milage left = milages.get(i - 1);
		map.put(
			left.getRecordedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
			milages.get(i).getAmount().subtract(left.getAmount()).intValue()
		);
	    }, HashMap::putAll);
	}

	return this.periods;
    }

    /**
     * Example of reducing a stream to a skalar value
     *
     * @return
     */
    public Integer getMilage() {
	return this.getPeriods().values().parallelStream().collect(reducing(Integer::sum)).get();
    }

    /**
     * An example of a nullable Optional
     *
     * @param period
     * @return
     */
    public Integer getMilageInPeriod(final LocalDate period) {
	return Optional.ofNullable(this.getPeriods().get(period)).orElse(0);
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
	return Objects.equals(this.id, other.id);
    }
}
