/*
 * Copyright 2014-2016 Michael J. Simons.
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
package ac.simons.biking2.galleryPictures;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
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
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Michael J. Simons, 2014-02-22
 */
@Entity
@Table(name = "gallery_pictures")
class GalleryPictureEntity implements Serializable {

    private static final long serialVersionUID = -5303688860568518942L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "taken_on", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar takenOn;

    @Column(name = "filename", length = 36, unique = true, updatable = false)
    @NotNull
    @Size(max = 36)
    private String filename;

    @Column(name = "description", length = 2048, nullable = false)
    @NotBlank
    @Size(max = 2048)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @JsonIgnore
    private Calendar createdAt;

    protected GalleryPictureEntity() {
    }

    GalleryPictureEntity(final Calendar takenOn, final String filename) {
        this.takenOn = takenOn;
        this.filename = filename;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Calendar.getInstance();
        }
    }

    public Integer getId() {
        return id;
    }

    public Calendar getTakenOn() {
        return takenOn;
    }

    public String getFilename() {
        return filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.takenOn);
        hash = 23 * hash + Objects.hashCode(this.filename);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GalleryPictureEntity other = (GalleryPictureEntity) obj;
        if (!Objects.equals(this.takenOn, other.takenOn)) {
            return false;
        }
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        return true;
    }
}
