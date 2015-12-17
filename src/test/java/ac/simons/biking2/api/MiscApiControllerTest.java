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
package ac.simons.biking2.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-02-17
 */
public class MiscApiControllerTest {
    private final Coordinate home = new Coordinate("13.408056", "52.518611");

    @Test
    public void testGetHome() {
	final MiscApiController controller = new MiscApiController(this.home);
	
	Assert.assertEquals(this.home, controller.getHome());
    }
}
