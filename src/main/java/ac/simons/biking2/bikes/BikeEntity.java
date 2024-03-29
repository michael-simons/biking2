/*
 * Copyright 2014-2023 michael-simons.eu.
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.Formula;
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
@SuppressWarnings("JpaObjectClassSignatureInspection")
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bike")
    @OrderBy("recordedOn asc")
    @JsonIgnore
    private final List<MilageEntity> milages = new ArrayList<>();

    @Column(name = "last_milage", nullable = false, precision = 8, scale = 2)
    @NotNull
    @JsonIgnore
    private BigDecimal lastMilage = BigDecimal.ZERO;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bike")
    @OrderBy("lentOn asc, returnedOn asc")
    @JsonIgnore
    private final List<LentMilageEntity> lentMilages = new ArrayList<>();

    @Formula("SELECT coalesce(sum(l.amount), 0.0) FROM lent_milages l WHERE l.bike_id = id")
    @JsonIgnore
    private BigDecimal lentMilage = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    @NotNull
    @JsonIgnore
    @Getter
    private OffsetDateTime createdAt;

    @Embedded
    @Getter
    @Setter
    private Link story;

    @Column(nullable = false)
    @JsonIgnore
    @Getter
    @Setter
    private boolean miscellaneous = false;

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
            var lastRecordedMilage = this.milages.get(this.milages.size() - 1);
            var nextValidDate = lastRecordedMilage.getRecordedOn().plusMonths(1);
            if (!recordedOn.equals(nextValidDate)) {
                throw new IllegalArgumentException("Next valid date for milage is " + nextValidDate);
            }
            if (lastRecordedMilage.getAmount().doubleValue() > amount) {
                throw new IllegalArgumentException("New amount must be greater than or equal " + lastRecordedMilage.getAmount().toPlainString());
            }
        }
        var newRecordedMilage = new MilageEntity(this, recordedOn.withDayOfMonth(1), amount);
        this.milages.add(newRecordedMilage);
        this.lastMilage = newRecordedMilage.getAmount();
        return newRecordedMilage;
    }

    public synchronized LentMilageEntity lent(final LocalDate lentOn) {

        boolean isLent = !this.lentMilages.isEmpty() && this.lentMilages.stream().anyMatch(LentMilageEntity::isLent);
        if (isLent) {
            throw new IllegalStateException("Bike is already lent");
        }

        var lentMilageEntity = new LentMilageEntity(this, lentOn);
        this.lentMilages.add(lentMilageEntity);
        return lentMilageEntity;
    }

    public synchronized LentMilageEntity returnIt(final LocalDate returnedOn, final double amount) {

        return this.lentMilages.stream()
                .filter(LentMilageEntity::isLent).findFirst()
                .map(l -> {
                    l.setReturnedOn(returnedOn);
                    l.setAmount(BigDecimal.valueOf(amount));
                    this.lentMilage = this.lentMilages.stream().map(LentMilageEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return l;
                }).orElseThrow(() -> new IllegalStateException("Bike is not lent."));
    }

    /**
     * @return The last milage recorded for this bike.
     */
    @JsonProperty
    public int getLastMilage() {
        return this.lastMilage.add(this.lentMilage).intValue();
    }
}
