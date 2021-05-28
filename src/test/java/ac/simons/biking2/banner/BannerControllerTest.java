/*
 * Copyright 2016-2019 michael-simons.eu.
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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ac.simons.biking2.config.SecurityConfig;

/**
 * @author Michael J. Simons
 *
 * @since 2016-04-15
 */
@WebMvcTest(
        includeFilters = @Filter(type = ASSIGNABLE_TYPE, value = SecurityConfig.class),
        controllers = BannerController.class
)
@AutoConfigureRestDocs(
        outputDir = "target/generated-snippets",
        uriHost = "biking.michael-simons.eu",
        uriPort = 80
)
class BannerControllerTest {

    @MockBean
    private Banner banner;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getShouldReturnBanner() throws Exception {
        doAnswer(invocation -> {
            final PrintStream out = invocation.getArgument(2);
            out.write(bannerText.getBytes());
            return null;
        }).when(banner).printBanner(any(), any(), any());

        mockMvc
                .perform(
                        get("/api/banner").accept(TEXT_PLAIN)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(bannerText))
                .andDo(document("api/banner",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    private final String bannerText = """
           ▄▄▄▄▄   █ ▄▄  █▄▄▄▄ ▄█    ▄     ▄▀      █▄▄▄▄ ▄███▄     ▄▄▄▄▄      ▄▄▄▄▀     ██▄   ████▄ ▄█▄      ▄▄▄▄▄   
          █     ▀▄ █   █ █  ▄▀ ██     █  ▄▀        █  ▄▀ █▀   ▀   █     ▀▄ ▀▀▀ █        █  █  █   █ █▀ ▀▄   █     ▀▄ 
        ▄  ▀▀▀▀▄   █▀▀▀  █▀▀▌  ██ ██   █ █ ▀▄      █▀▀▌  ██▄▄   ▄  ▀▀▀▀▄       █        █   █ █   █ █   ▀ ▄  ▀▀▀▀▄   
         ▀▄▄▄▄▀    █     █  █  ▐█ █ █  █ █   █     █  █  █▄   ▄▀ ▀▄▄▄▄▀       █         █  █  ▀████ █▄  ▄▀ ▀▄▄▄▄▀    
                    █      █    ▐ █  █ █  ███        █   ▀███▀               ▀          ███▀        ▀███▀            
                     ▀    ▀       █   ██            ▀                                                                
        """;
}
