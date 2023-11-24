/*
 * Copyright 2016-2023 michael-simons.eu.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Supports sending files either through Tomcats sendfile support or through
 * streaming them into an outputstream.
 *
 * @author Michael J. Simons, 2016-08-03
 */
@RequiredArgsConstructor
public final class FileBasedResource {

    /**
     * The file to send.
     */
    private final File imageFile;

    /**
     * The filename used in the header.
     */
    private final String filename;

    /**
     * How long should the response be cached.
     */
    private final int cacheForDays;

    /**
     * Sends the file. The responses buffer will be flushed.
     *
     * @param request Needed for determining wether sendfile is supported or not
     * @param response The response to write to
     * @throws java.io.IOException If response cannot be written to
     */
    public void send(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", "image/jpeg");
        response.setHeader("Content-Disposition", String.format("inline; filename=\"%s\"", this.filename));
        response.setHeader("Expires", ZonedDateTime.now(ZoneId.of("UTC")).plusDays(cacheForDays).format(RFC_1123_DATE_TIME.withLocale(Locale.US)));
        response.setHeader("Cache-Control", String.format("max-age=%d, %s", TimeUnit.DAYS.toSeconds(cacheForDays), "public"));

        // Attribute maybe null
        if (request == null || !Boolean.TRUE.equals(request.getAttribute("org.apache.tomcat.sendfile.support"))) {
            Files.copy(this.imageFile.toPath(), response.getOutputStream());
            response.getOutputStream().flush();
        } else {
            long l = this.imageFile.length();
            request.setAttribute("org.apache.tomcat.sendfile.filename", this.imageFile.getAbsolutePath());
            request.setAttribute("org.apache.tomcat.sendfile.start", 0L);
            request.setAttribute("org.apache.tomcat.sendfile.end", l);
            response.setHeader("Content-Length", Long.toString(l));
        }
        response.flushBuffer();
    }
}
