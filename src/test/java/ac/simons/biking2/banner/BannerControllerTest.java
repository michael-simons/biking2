/*
 * Copyright 2016-2017 michael-simons.eu.
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons, 2016-04-15
 */
@RunWith(SpringRunner.class)
@WebMvcTest(
        controllers = BannerController.class,
        secure = false
)
@AutoConfigureRestDocs(
        outputDir = "target/generated-snippets",
        uriHost = "biking.michael-simons.eu",
        uriPort = 80
)
public class BannerControllerTest {

    @MockBean
    private Banner banner;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSomeMethod() throws Exception {
        doAnswer(invocation -> {
            final PrintStream out = invocation.getArgument(2);
            out.write(bannerText.getBytes());
            return null;
        }).when(banner).printBanner(any(), any(), any());

        mockMvc
                .perform(
                        get("/api/banner").accept(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(bannerText))
                .andDo(document("api/banner",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    private final String bannerText
            = " _____            _              ______ _____ _____ _____       _                \n"
            + "/  ___|          (_)             | ___ \\  ___/  ___|_   _|     | |               \n"
            + "\\ `--. _ __  _ __ _ _ __   __ _  | |_/ / |__ \\ `--.  | |     __| | ___   ___ ___ \n"
            + " `--. \\ '_ \\| '__| | '_ \\ / _` | |    /|  __| `--. \\ | |    / _` |/ _ \\ / __/ __|\n"
            + "/\\__/ / |_) | |  | | | | | (_| | | |\\ \\| |___/\\__/ / | |   | (_| | (_) | (__\\__ \\\n"
            + "\\____/| .__/|_|  |_|_| |_|\\__, | \\_| \\_\\____/\\____/  \\_/    \\__,_|\\___/ \\___|___/\n"
            + "      | |                  __/ |                                                 \n"
            + "      |_|                 |___/                                                  \n"
            + "          _ _   _        ___   _____ _____ _____ _____              _            \n"
            + "         (_) | | |      / _ \\ /  ___/  __ \\_   _|_   _|            | |           \n"
            + "__      ___| |_| |__   / /_\\ \\\\ `--.| /  \\/ | |   | |     __ _ _ __| |_          \n"
            + "\\ \\ /\\ / / | __| '_ \\  |  _  | `--. \\ |     | |   | |    / _` | '__| __|         \n"
            + " \\ V  V /| | |_| | | | | | | |/\\__/ / \\__/\\_| |_ _| |_  | (_| | |  | |_          \n"
            + "  \\_/\\_/ |_|\\__|_| |_| \\_| |_/\\____/ \\____/\\___/ \\___/   \\__,_|_|   \\__|         \n"
            + "                                                                                 \n"
            + "                                                                                 ";
}
