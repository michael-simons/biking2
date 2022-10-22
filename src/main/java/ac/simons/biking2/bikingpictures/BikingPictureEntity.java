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
package ac.simons.biking2.bikingpictures;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "biking_pictures")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode(of = "externalId")
class BikingPictureEntity implements Serializable {

    private static final long serialVersionUID = -7050582813676065697L;

    private static final Pattern GUID_PATTERN = Pattern.compile("https?://dailyfratze.de/fratzen/m/(\\d+).jpg");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The url of the image itself
     */
    @Column(name = "external_id", nullable = false, unique = true)
    @NotNull
    private Integer externalId;

    @Column(name = "pub_date", nullable = false)
    @NotNull
    private OffsetDateTime pubDate;

    /**
     * A link to the webpage
     */
    @Column(nullable = false, length = 512)
    @URL
    @NotNull
    @Size(max = 512)
    private String link;

    BikingPictureEntity(final String guid, final ZonedDateTime pubDate, final String link) {
        final Matcher matcher = GUID_PATTERN.matcher(guid);
        if (!matcher.matches()) {
            throw new InvalidGUIDException();
        }
        this.externalId = Integer.parseInt(matcher.group(1));
        this.pubDate = pubDate.toOffsetDateTime();
        this.link = link;
    }
}
