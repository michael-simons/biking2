/*
 * Copyright 2014-2019 michael-simons.eu.
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
package ac.simons.biking2.bikes;

import static ac.simons.biking2.bikes.BikesController.Messages.ALREADY_DECOMMISSIONED;
import static ac.simons.biking2.shared.Messages.INVALID_ARGUMENTS;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Michael J. Simons
 * @since 2014-02-19
 */
@RestController
@RequestMapping("/api")
class BikesController {

    enum Messages {

        ALREADY_DECOMMISSIONED("alreadyDecommissioned");

        public final String key;

        Messages(final String key) {
            this.key = "bikes." + key;
        }
    }

    private final BikeService bikeService;

    private final MessageSourceAccessor i18n;

    BikesController(final BikeService bikeService, final MessageSource messageSource) {
        this.bikeService = bikeService;
        this.i18n = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
    }

    @RequestMapping(value = "/bikes", method = GET)
    public List<BikeEntity> getBikes(@RequestParam(required = false, defaultValue = "false") final boolean all) {

        return this.bikeService.getBikes(all);
    }

    @RequestMapping(value = "/bikes/{id:\\d+}/milages", method = POST)
    @PreAuthorize("isAuthenticated()")
    public MilageEntity createMilage(@PathVariable final Integer id, @RequestBody @Valid final NewMilageCmd cmd, final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(i18n.getMessage(INVALID_ARGUMENTS.key));
        }

        try {
            return this.bikeService.createMilage(id, cmd);
        } catch (BikeService.BikeNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (BikeService.BikeAlreadyDecommissionedException e) {
            throw new IllegalArgumentException(i18n.getMessage(ALREADY_DECOMMISSIONED.key));
        }
    }

    @RequestMapping(value = "/bikes", method = POST)
    @PreAuthorize("isAuthenticated()")
    public BikeEntity createBike(@RequestBody @Valid final BikeCmd newBike, final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(i18n.getMessage(INVALID_ARGUMENTS.key));
        }

        return bikeService.createBike(newBike);
    }

    @RequestMapping(value = "/bikes/{id:\\d+}", method = PUT)
    @PreAuthorize("isAuthenticated()")
    public BikeEntity updateBike(@PathVariable final Integer id, @RequestBody @Valid final BikeCmd updatedBike, final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(i18n.getMessage(INVALID_ARGUMENTS.key));
        }

        try {
            return this.bikeService.updateBike(id, updatedBike);
        } catch (BikeService.BikeNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (BikeService.BikeAlreadyDecommissionedException e) {
            throw new IllegalArgumentException(i18n.getMessage(ALREADY_DECOMMISSIONED.key));
        }
    }

    @RequestMapping(value = "/bikes/{id:\\d+}/story", method = PUT)
    @PreAuthorize("isAuthenticated()")
    public BikeEntity updateBikeStory(@PathVariable final Integer id, @RequestBody(required = false) @Valid final StoryCmd newStory, final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(i18n.getMessage(INVALID_ARGUMENTS.key));
        }

        try {
            return this.bikeService.updateBikeStory(id, newStory);
        } catch (BikeService.BikeNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (BikeService.BikeAlreadyDecommissionedException e) {
            throw new IllegalArgumentException(i18n.getMessage(ALREADY_DECOMMISSIONED.key));
        }
    }
}
