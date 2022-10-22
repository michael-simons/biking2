/*
 * Copyright 2016-2018 michael-simons.eu.
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

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Michael J. Simons, 2016-04-15
 */
@Controller
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@RequestMapping("/api/banner")
class BannerController {

    private final Banner banner;

    private final Environment environment;

    @GetMapping(produces = TEXT_PLAIN_VALUE)
    public void get(final HttpServletResponse response) throws IOException {
        final String charset = StandardCharsets.UTF_8.name();
        response.setHeader("Content-Type", String.format("%s;charset=%s", TEXT_PLAIN_VALUE, charset));
        try (PrintStream printStream = new PrintStream(response.getOutputStream(), false, charset)) {
            banner.printBanner(environment, BannerController.class, printStream);
        }
    }
}
