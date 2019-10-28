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

CREATE TABLE gallery_pictures (
  id                  serial primary key,
  taken_on            DATE NOT NULL,
  filename            VARCHAR(36) NOT NULL,
  description         VARCHAR(2048) NOT NULL,
  created_at          DATETIME NOT NULL,
  CONSTRAINT filename_unique UNIQUE(filename)
);               
