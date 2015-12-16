/*
 * Copyright 2015 Michael J. Simons.
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
package ac.simons.biking2.tests;

import java.util.Locale;
import java.util.function.BiConsumer;
import org.joor.Reflect;
import org.junit.Assert;

/**
 * @author Michael J. Simons, 2015-09-17
 */
public class BeanTester implements BiConsumer<String, Object> {

    private final Reflect r;

    public BeanTester(Class<?> clazz) {
	this.r = Reflect.on(clazz).create();
    }

    @Override
    public void accept(String p, Object v) {
	final String property = p.substring(0, 1).toUpperCase(Locale.ENGLISH) + p.substring(1);
	final String verbSet = "set";
	final String verbGet = v instanceof Boolean ? "is" : "get";
	try {
	    Assert.assertEquals(v, r.call(verbSet + property, v).call(verbGet + property).get());
	} catch(Exception e) {
	    e.printStackTrace();
	    Assert.fail(e.getMessage());
	}
    }
}
