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
package ac.simons.biking2.tracks;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Michael J. Simons, 2014-02-17
 */
@Component("home")
class Coordinate {

    private final BigDecimal longitude;
    private final BigDecimal latitude;

    @Autowired
    public Coordinate(
	    @Value("${biking2.home.longitude}") final String longitude,
	    @Value("${biking2.home.latitude}") final String latitude
    ) {
	this(new BigDecimal(longitude), new BigDecimal(latitude));
    }

    public Coordinate(BigDecimal longitude, BigDecimal latitude) {
	this.longitude = longitude;
	this.latitude = latitude;
    }

    public double getLatitude() {
	return latitude.doubleValue();
    }

    public double getLongitude() {
	return longitude.doubleValue();
    }
}
