/*
 * Copyright 2014-2016 michael-simons.eu.
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
package ac.simons.biking2.bikingpictures.rss;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Michael J. Simons, 2014-02-17
 */
public final class RSSDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    @Override
    public ZonedDateTime unmarshal(final String v) {
        return ZonedDateTime.parse(v, formatter);
    }

    @Override
    public String marshal(final ZonedDateTime v) {
        return v.format(formatter);
    }
}
