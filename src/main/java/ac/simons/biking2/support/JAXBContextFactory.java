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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * I don't want to deal with the checked {@link JAXBException} in the calling
 * class so this is a wrapper that handles those for me. I don't expect any
 * exception to be thrown
 *
 * @author Michael J. Simons, 2014-05-23
 */
@RequiredArgsConstructor
public final class JAXBContextFactory {

    private final Class<?> baseClass;

    @SneakyThrows
    public JAXBContext createContext() {
        return JAXBContext.newInstance(this.baseClass);
    }
}
