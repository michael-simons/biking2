/*
 * Copyright 2014 michael-simons.eu.
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

package ac.simons.biking2.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-05-20
 */
public class NewLocationCmdTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * The command is primarly used through json mapping, so i choose
     * the same approach here.
     */
    @Test
    public void beanShouldWorkAsExpected() throws IOException {	
	NewLocationCmd newLocationCmd = objectMapper.readValue("{\"lon\":\"5\", \"lat\":\"50\"}", NewLocationCmd.class);
	Assert.assertEquals(BigDecimal.valueOf(5l), newLocationCmd.getLongitude());
	Assert.assertEquals(BigDecimal.valueOf(50l), newLocationCmd.getLatitude());
	Assert.assertNull(newLocationCmd.getCreatedAt());
	
	newLocationCmd = objectMapper.readValue("{\"lon\":\"5\", \"lat\":\"50\", \"tst\": \"1400577777\"}", NewLocationCmd.class);
	Calendar hlp = Calendar.getInstance();
	hlp.setTime(Date.from(ZonedDateTime.of(2014, 5, 20, 11, 22, 57, 0, ZoneId.systemDefault()).toInstant()));
	Assert.assertEquals(hlp, newLocationCmd.getCreatedAt());
	
	newLocationCmd = objectMapper.readValue("{\"lon\":\"5\", \"lat\":\"50\", \"tstMillis\": \"1400578694000\"}", NewLocationCmd.class);
	hlp = Calendar.getInstance();
	hlp.setTime(Date.from(ZonedDateTime.of(2014, 5, 20, 11, 38, 14, 0, ZoneId.systemDefault()).toInstant()));
	Assert.assertEquals(hlp, newLocationCmd.getCreatedAt());	
	
	newLocationCmd = objectMapper.readValue("{\"lon\":\"5\", \"lat\":\"50\", \"tst\": \"1400577777\", \"tstMillis\": \"1400578694000\"}", NewLocationCmd.class);
	hlp = Calendar.getInstance();
	hlp.setTime(Date.from(ZonedDateTime.of(2014, 5, 20, 11, 22, 57, 0, ZoneId.systemDefault()).toInstant()));
	Assert.assertEquals(hlp, newLocationCmd.getCreatedAt());
    }   
}
