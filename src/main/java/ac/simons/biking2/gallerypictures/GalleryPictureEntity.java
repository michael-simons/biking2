/*
 * Copyright 2014-2017 michael-simons.eu.
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
package ac.simons.biking2.gallerypictures;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Michael J. Simons, 2014-02-22
 */
@Entity
@Table(name = "gallery_pictures")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode(of = {"takenOn", "filename"})
class GalleryPictureEntity implements Serializable {

    private static final long serialVersionUID = -5303688860568518942L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "taken_on", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar takenOn;

    @Column(length = 36, unique = true, updatable = false)
    @NotNull
    @Size(max = 36)
    private String filename;

    @Column(length = 2048, nullable = false)
    @NotBlank
    @Size(max = 2048)
    @Setter
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @JsonIgnore
    private Calendar createdAt;

    GalleryPictureEntity(final Calendar takenOn, final String filename) {
        this.takenOn = takenOn;
        this.filename = filename;
        this.createdAt = Calendar.getInstance();
    }
}
