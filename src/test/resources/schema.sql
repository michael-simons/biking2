CREATE TABLE assorted_trips (
    id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, 
    covered_on DATE NOT NULL,
    distance DECIMAL(8,2) NOT NULL
);

CREATE TABLE bikes (
    id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, 
    name VARCHAR(255) NOT NULL, 
    bought_on DATE NOT NULL,
    color VARCHAR(6) NOT NULL DEFAULT 'CCCCCC', 
    decommissioned_on DATE,
    created_at DATETIME NOT NULL,         
    CONSTRAINT bikes_unique_name UNIQUE(name),
);

CREATE TABLE biking_pictures (
    id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, 
    external_id INTEGER NOT NULL, 
    pub_date DATETIME NOT NULL,
    link VARCHAR(512) NOT NULL,
    CONSTRAINT biking_picture_unique_external_id UNIQUE(external_id),
);

CREATE TABLE locations (
    id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, 
    created_at DATETIME NOT NULL, 
    latitude DECIMAL(18,15) NOT NULL, 
    longitude DECIMAL(18,15) NOT NULL, 
    description VARCHAR(2048)
);

CREATE TABLE milages (
    id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, 
    recorded_on DATE NOT NULL, 
    amount DECIMAL(8,2) NOT NULL, 
    created_at DATETIME NOT NULL, 
    bike_id INTEGER NOT NULL,
    CONSTRAINT milage_unique UNIQUE(bike_id, recorded_on),
    FOREIGN KEY (bike_id) REFERENCES bikes(id) ON DELETE CASCADE
);

CREATE TABLE tracks (
    id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, 
    name VARCHAR(512) NOT NULL, 
    covered_on DATE NOT NULL, 
    description VARCHAR(2048), 
    minlat DECIMAL(18, 15), 
    minlon DECIMAL(18, 15), 
    maxlat DECIMAL(18, 15), 
    maxlon DECIMAL(18, 15), 
    type VARCHAR(50) DEFAULT 'biking' NOT NULL,
    CONSTRAINT track_unique UNIQUE(covered_on, name),
    CONSTRAINT track_check_type CHECK (type IN ('biking', 'running'))
);