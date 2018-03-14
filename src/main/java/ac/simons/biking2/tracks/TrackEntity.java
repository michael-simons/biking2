/*
 * Copyright 2014-2018 michael-simons.eu.
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
package ac.simons.biking2.tracks;

import ac.simons.biking2.config.DatastoreConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "tracks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"covered_on", "name"})
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode(of = {"name", "coveredOn"})
class TrackEntity implements Serializable {

    private static final long serialVersionUID = 7630613853916630933L;

    public enum Type {

        biking, running
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(length = 512, nullable = false)
    @NotBlank
    @Size(max = 512)
    private String name;

    @Column(name = "covered_on", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar coveredOn;

    @Column(length = 2048)
    @Setter
    private String description;

    @Column(precision = 18, scale = 15)
    @Setter
    private BigDecimal minlat;

    @Column(precision = 18, scale = 15)
    @Setter
    private BigDecimal minlon;

    @Column(precision = 18, scale = 15)
    @Setter
    private BigDecimal maxlat;

    @Column(precision = 18, scale = 15)
    @Setter
    private BigDecimal maxlon;

    @Enumerated(EnumType.STRING)
    @Setter
    private Type type = Type.biking;

    TrackEntity(final String name, final Calendar coveredOn) {
        this.name = name;
        this.coveredOn = coveredOn;
    }

    @JsonProperty("id")
    public String getPrettyId() {
        return this.getId() == null ? "" : Integer.toString(this.getId(), 36);
    }

    public File getTrackFile(final File datastoreBaseDirectory, final String format) {
        return new File(datastoreBaseDirectory, String.format("%s/%d.%s", DatastoreConfig.TRACK_DIRECTORY, this.getId(), format));
    }
}
