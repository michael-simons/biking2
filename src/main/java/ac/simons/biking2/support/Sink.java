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

package ac.simons.biking2.support;

/**
 * A generic Sink that consists of a target <em>T</em> that takes
 * an object <em>O</em> and is then returned
 * @author Michael J. Simons, 2014-02-11
 * @param <T>
 * @param <O>
 */
@FunctionalInterface
public interface Sink<T, O> {
    T setObject(final O object);
}
