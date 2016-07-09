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

package ac.simons.biking2.tracks;

import ac.simons.biking2.support.BeanTester;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-05-23
 */
public class OEmbedResponseTest {

    @Test
    public void beanShouldWorkAsExpected() {
        final Map<String, Object> values = new HashMap<>();
        values.put("type", "type");
        values.put("version", "version");
        values.put("title", "title");
        values.put("authorName", "authorName");
        values.put("authorUrl", "authorUrl");
        values.put("providerName", "providerName");
        values.put("providerUrl", "providerUrl");
        values.put("cacheAge", 123l);
        values.put("thumbnailUrl", "thumbnailUrl");
        values.put("thumbnailWidth", 23);
        values.put("thumbnailHeight", 23);
        values.put("url", "url");
        values.put("html", "html");
        values.put("width", 42);
        values.put("height", 42);

        values.forEach(new BeanTester(OEmbedResponse.class));
    }
}
