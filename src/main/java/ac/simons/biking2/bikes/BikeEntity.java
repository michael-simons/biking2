/*
 * Copyright 2014-2019 michael-simons.eu.
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Michael J. Simons
 * @since 2014-02-08
 */
@Entity
@Table(name = "bikes")
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "name")
public class BikeEntity implements Serializable {

    private static final long serialVersionUID = 1249824815158908981L;

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    @EqualsAndHashCode(of = "url")
    public static class Link implements Serializable {

        private static final long serialVersionUID = 4086706843689307842L;

        @Column(length = 512)
        @NotBlank
        @URL
        private String url;

        @Column(length = 255)
        @NotBlank
        @Setter
        private String label;

        public Link(final String url, final String label) {
            this.url = url;
            this.label = label;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Integer id;

    @Column(unique = true, length = 255, nullable = false)
    @NotBlank
    @Size(max = 255)
    @Getter
    private String name;

    @Column(length = 6, nullable = false)
    @NotBlank
    @Size(max = 6)
    @Getter
    @Setter
    private String color = "CCCCCC";

    @Column(name = "bought_on", nullable = false)
    @NotNull
    @Getter
    private LocalDate boughtOn;

    @Column(name = "decommissioned_on")
    @Getter
    private LocalDate decommissionedOn;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "bike")
    @OrderBy("recordedOn asc")
    @JsonIgnore
    private final List<MilageEntity> milages = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @NotNull
    @JsonIgnore
    @Getter
    private OffsetDateTime createdAt;

    @Embedded
    @Getter
    @Setter
    private Link story;

    public BikeEntity(final String name, final LocalDate boughtOn) {

        this.name = name;
        this.boughtOn = boughtOn;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * @param decommissionDate Date of decommission. If null nothing changes
     * @return true if the bike was decommissioned
     */
    public boolean decommission(final LocalDate decommissionDate) {

        if (decommissionDate != null) {
            this.decommissionedOn = decommissionDate;
        }
        return this.decommissionedOn != null;
    }

    public synchronized MilageEntity addMilage(final LocalDate recordedOn, final double amount) {

        if (!this.milages.isEmpty()) {
            final MilageEntity lastMilage = this.milages.get(this.milages.size() - 1);
            LocalDate nextValidDate = lastMilage.getRecordedOn().plusMonths(1);
            if (!recordedOn.equals(nextValidDate)) {
                throw new IllegalArgumentException("Next valid date for milage is " + nextValidDate);
            }
            if (lastMilage.getAmount().doubleValue() > amount) {
                throw new IllegalArgumentException("New amount must be greater than or equal " + lastMilage.getAmount().toPlainString());
            }
        }
        final MilageEntity milage = new MilageEntity(this, recordedOn.withDayOfMonth(1), amount);
        this.milages.add(milage);
        return milage;
    }

    /**
     * @return The total milage that has been recorded here.
     */
    @JsonProperty
    public int getMilage() {

        if (this.milages.isEmpty()) {
            return 0;
        }

        return this.getLastMilage() - this.getFirstMilage();
    }

    /**
     * @return The first milage recorded with this app.
     */
    int getFirstMilage() {
        return this.milages.isEmpty() ? 0 : this.milages.get(0).getAmount().intValue();
    }

    /**
     * @return The last milage recorded with this app.
     */
    @JsonProperty
    public int getLastMilage() {
        return this.milages.isEmpty() ? 0 : this.milages.get(this.milages.size() - 1).getAmount().intValue();
    }

    public boolean hasMilages() {
        return !this.milages.isEmpty();
    }
}
