/*
 * Copyright 2021 michael-simons.eu.
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

CREATE TABLE lent_milages (
  id                  serial primary key,
  lent_on             DATE NOT NULL,
  returned_on         DATE,
  amount              DECIMAL(8, 2) NOT NULL,
  created_at          DATETIME NOT NULL,
  bike_id             INTEGER NOT NULL,
  CONSTRAINT lent_milage_unique UNIQUE(bike_id, lent_on),
  CONSTRAINT lent_milage_bike_fk FOREIGN KEY(bike_id) REFERENCES bikes(id) ON DELETE CASCADE
);
