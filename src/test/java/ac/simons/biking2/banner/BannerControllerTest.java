/*
 * Copyright 2016 michael-simons.eu.
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
package ac.simons.biking2.banner;

import java.io.PrintStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons, 2016-04-15
 */
@RunWith(SpringRunner.class)
@WebMvcTest(
	controllers = BannerController.class,
	excludeFilters = {
	    @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)
	}
)
@MockBean(Banner.class)
public class BannerControllerTest {

    @Autowired
    private Banner banner;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSomeMethod() throws Exception {
	doAnswer(new Answer<Void>() {
	    @Override
	    public Void answer(InvocationOnMock invocation) throws Throwable {
		final PrintStream out = invocation.getArgumentAt(2, PrintStream.class);
		out.write("Hallo, Welt".getBytes());
		return null;
	    }
	}).when(banner).printBanner(anyObject(), anyObject(), anyObject());
	mockMvc
		.perform(
			get("http://biking.michael-simons.eu/api/banner").accept(APPLICATION_JSON)
		)
		.andExpect(status().isOk())
		.andExpect(content().string("Hallo, Welt"));
    }

}
