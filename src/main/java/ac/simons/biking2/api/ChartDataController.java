/*
 * Copyright 2014 msimons.
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

import ac.simons.biking2.persistence.entities.Bike;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

/**
 * @author msimons, 2014-02-09
 */
public class ChartDataController {
   private final BikeRepository bikeRepository;
   
   @Autowired
   public ChartDataController(final BikeRepository bikeRepository) {
       this.bikeRepository = bikeRepository;
   }
   
   public void getCurrentData() {
       // final List<Bike> bikes = this.bikeRepository.findAll();
       final LocalDate january = LocalDate.now().withMonth(1).withDayOfMonth(1);
       
       final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMM");
       
       IntStream
               .rangeClosed(1, 12).mapToObj(i -> january.withMonth(i).format(dateTimeFormat))
               .forEach(System.out::println);
   }
}
