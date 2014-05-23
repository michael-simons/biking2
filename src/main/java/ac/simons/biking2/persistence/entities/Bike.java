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

import ac.simons.biking2.misc.AccumulatedPeriod;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

import static java.util.stream.Collectors.reducing;
import static java.util.stream.IntStream.rangeClosed;

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
    ),
    @NamedQuery(
	    name = "Bike.getDateOfFirstRecord",
	    query
	    = "Select coalesce(min(m.recordedOn), current_date()) from Milage m"
    )
})
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
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

    @Column(name = "bought_on", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar boughtOn;
    
    @Column(name = "decommissioned_on")
    @Temporal(TemporalType.DATE)
    private Calendar decommissionedOn;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "bike")
    @OrderBy("recordedOn asc")
    @JsonIgnore
    private final List<Milage> milages = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @JsonIgnore
    private Calendar createdAt;

    /**
     * Contains all monthly periods that bike has been used
     */
    @JsonIgnore
    private transient Map<LocalDate, Integer> periods;

    protected Bike() {
    }

    public Bike(String name, final LocalDate boughtOn) {
	this.name = name;
	this.boughtOn = GregorianCalendar.from(boughtOn.atStartOfDay(ZoneId.systemDefault()));	
    }

    @PrePersist
    public void prePersist() {
	if (this.createdAt == null) {
	    this.createdAt = Calendar.getInstance();
	}
    }

    public Integer getId() {
	return this.id;
    }

    public String getName() {
	return this.name;
    }

    public String getColor() {
	return this.color;
    }

    public void setColor(String color) {
	this.color = color;
    }

    public Calendar getDecommissionedOn() {
	return this.decommissionedOn;
    }

    public void setDecommissionedOn(Calendar decommissionedOn) {
	this.decommissionedOn = decommissionedOn;
    }

    public Calendar getCreatedAt() {
	return this.createdAt;
    }
  
    public synchronized Milage addMilage(final LocalDate recordedOn, final double amount) {
	if(this.milages.size() > 0) {
	    final Milage lastMilage = this.milages.get(this.milages.size() - 1);
	    LocalDate nextValidDate = lastMilage.getRecordedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusMonths(1);
	    if(!recordedOn.equals(nextValidDate))
		throw new IllegalArgumentException("Next valid date for milage is " + nextValidDate);
	    if(lastMilage.getAmount().doubleValue() > amount)
		throw new IllegalArgumentException("New amount must be greater than or equal " + lastMilage.getAmount().toPlainString());
	}
	final Milage milage = new Milage(this, recordedOn.withDayOfMonth(1), amount);
	this.milages.add(milage);	
	this.periods = null;	
	return milage;
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
	    this.periods = IntStream.range(1, this.milages.size()).collect(TreeMap::new, (map, i) -> {
		final Milage left = milages.get(i - 1);
		map.put(
			left.getRecordedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
			milages.get(i).getAmount().subtract(left.getAmount()).intValue()
		);
	    }, TreeMap::putAll);
	}

	return this.periods;
    }
        
    /**
     * Example of reducing a stream to a skalar value
     *
     * @return
     */
    @JsonProperty
    public int getMilage() {
	return this.getPeriods().values().parallelStream().collect(reducing(Integer::sum)).orElse(0);
    }
    
    @JsonProperty
    public int getLastMilage() {	
	return this.milages == null || this.milages.isEmpty() ? 0 : this.milages.get(this.milages.size()-1).getAmount().intValue();
    }
    
    /**
     * An example of a nullable Optional
     *
     * @param period
     * @return
     */
    public int getMilageInPeriod(final LocalDate period) {
	return Optional.ofNullable(this.getPeriods().get(period)).orElse(0);
    }
    
    /**
     * Returns an array with 12 elements, containing the milages for each month
     * of the given year
     * 
     * @param year The year in which the milages should be computed
     * @return 12 values of milages for the month of the given year
     */
    public int[] getMilagesInYear(int year) {	
	final LocalDate january1st = LocalDate.of(year, Month.JANUARY, 1);
	// The limit is necessary because the range contains 13 elements for 
	// computing the correct periods, the last element is January 1st of year +1
	return rangeClosed(0, 12).map(i -> getMilageInPeriod(january1st.plusMonths(i))).limit(12).toArray();
    }
    
    /**
     * Returns the sum of all milages in the given year
     * 
     * @param year The year for which the sum of milages should be computed
     * @return  The sum of all milages in the given year
     */
    public int getMilageInYear(int year) {	
	return Arrays.stream(getMilagesInYear(year)).sum();
    }

    public Calendar getBoughtOn() {
	return boughtOn;
    }

    public void setBoughtOn(Calendar boughtOn) {
	this.boughtOn = boughtOn;
    }

    public boolean hasMilages() {
	return this.milages != null && this.milages.size() > 0;
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 97 * hash + Objects.hashCode(this.name);
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
	if (!Objects.equals(this.name, other.name)) {
	    return false;
	}
	return true;
    }
    
    public static int comparePeriodsByValue(Map.Entry<LocalDate, Integer> period1, Map.Entry<LocalDate, Integer> period2) {
	return Integer.compare(period1.getValue(), period2.getValue());
    }
    
    public static class BikeByMilageInYearComparator implements Comparator<Bike> {
	private final int year;

	public BikeByMilageInYearComparator(int year) {
	    this.year = year;
	}
	
	@Override
	public int compare(Bike o1, Bike o2) {
	    return Integer.compare(o1.getMilageInYear(year), o2.getMilageInYear(year));
	}
    }
    
    /**
     * This method groups all periods of the given bikes by their period start and
     * summarizes the value
     * 
     * @param bikes A likst of bikes whose milage periods should be grouped together
     * @param entryFilter An optional filter for the entries
     * @return A map of grouped periods
     */
    public static Map<LocalDate, Integer> summarizePeriods(final List<Bike> bikes, final Predicate<Map.Entry<LocalDate, Integer>> entryFilter) {	
	return bikes.stream()
	    .filter(Bike::hasMilages)
	    .flatMap(bike -> bike.getPeriods().entrySet().stream())			
	    .filter(Optional.ofNullable(entryFilter).orElse(entry -> true))
	    .collect(
		Collectors.groupingBy(
		    Map.Entry::getKey,
		    Collectors.reducing(0, Map.Entry::getValue, Integer::sum)
		)
	    );
    }    
    
    /**
     * Returns the worst performing period in the list of summarized (grouped) periods
     * 
     * @param summarizedPeriods A list of grouped periods
     * @return The worst (with the lowest value) period
     */
    public static AccumulatedPeriod getWorstPeriod(final Map<LocalDate, Integer> summarizedPeriods) {
	 return summarizedPeriods
			.entrySet()
			.stream()
			.min(Bike::comparePeriodsByValue)
			.map(entry -> new AccumulatedPeriod(entry.getKey(), entry.getValue()))
			.orElse(null);
    }
    
    /**
     * Returns the best performing period in the list of summarized (grouped) periods
     * 
     * @param summarizedPeriods A list of grouped periods
     * @return The best (with the highest value) period
     */
    public static AccumulatedPeriod getBestPeriod(final Map<LocalDate, Integer> summarizedPeriods) {
	 return summarizedPeriods
			.entrySet()
			.stream()
			.max(Bike::comparePeriodsByValue)
			.map(entry -> new AccumulatedPeriod(entry.getKey(), entry.getValue()))
			.orElse(null);
    }
}
