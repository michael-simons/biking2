/*
 * Copyright 2019 michael-simons.eu.
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

ALTER TABLE bikes ADD COLUMN last_milage DECIMAL(8, 2) DEFAULT 0 NOT NULL;

UPDATE bikes
SET last_milage = (
  SELECT amount FROM milages m WHERE m.bike_id = bikes.id ORDER BY recorded_on DESC OFFSET 0 FETCH NEXT ROW ONLY
);
