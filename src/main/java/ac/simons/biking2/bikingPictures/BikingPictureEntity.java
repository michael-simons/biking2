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
package ac.simons.biking2.bikingPictures;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Entity
@Table(name = "biking_pictures")
@NamedQueries({
     @NamedQuery(
	    name = "BikingPictureEntity.getMaxPubDate",
	    query
	    = "Select coalesce(max(bp.pubDate), '2005-08-07 18:30:42') from BikingPictureEntity bp"
    )
})
class BikingPictureEntity implements Serializable {

    private static final long serialVersionUID = -7050582813676065697L;

    private static final Pattern GUID_PATTERN = Pattern.compile("https?://dailyfratze.de/fratzen/m/(\\d+).jpg");
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The url of the image itself */
    @Column(name = "external_id", nullable = false, unique = true)    
    @NotNull    
    private Integer externalId;
    
    @Column(name = "pub_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Calendar pubDate;
    
    /** A link to the webpage */
    @Column(name = "link", nullable = false, length = 512)
    @URL
    @NotNull
    @Size(max = 512)
    private String link;

    protected BikingPictureEntity() {
    }

    public BikingPictureEntity(final String guid, final ZonedDateTime pubDate, String link) {
	final Matcher matcher = GUID_PATTERN.matcher(guid);
	if(!matcher.matches())
	    throw new RuntimeException("Invalid GUID");	
	this.externalId = Integer.parseInt(matcher.group(1));
	this.pubDate = GregorianCalendar.from(pubDate);
	this.link = link;
    }

    public Integer getId() {
	return this.id;
    }

    public Integer getExternalId() {
	return externalId;
    }    

    public String getLink() {
	return this.link;
    }
   
    public Calendar getPubDate() {
	return pubDate;
    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 67 * hash + Objects.hashCode(this.externalId);
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
	final BikingPictureEntity other = (BikingPictureEntity) obj;
	return Objects.equals(this.externalId, other.externalId);
    }
}
