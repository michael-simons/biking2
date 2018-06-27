/*
 * Copyright 2017-2018 michael-simons.eu.
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
package ac.simons.biking2.support;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Michael J. Simons, 2017-04-19
 */
public class RegexMatcher extends BaseMatcher<String> {

    private final String regex;

    public RegexMatcher(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean matches(Object o) {
        return ((String) o).matches(regex);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("matches regex=" + this.regex);
    }

    public static RegexMatcher matches(String regex) {
        return new RegexMatcher(regex);
    }
}
