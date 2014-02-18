
insert into bikes(name, color, bought_on, decommissioned_on, created_at) values ('bike1', '000000', '2013-01-01', null, current_timestamp());
insert into bikes(name, color, bought_on, decommissioned_on, created_at) values ('bike2', '000000', '2012-01-01', '2012-09-30', current_timestamp());
insert into bikes(name, color, bought_on, decommissioned_on, created_at) values ('bike3', '000000', '2013-01-01', '2014-01-31', current_timestamp());
insert into bikes(name, color, bought_on, decommissioned_on, created_at) values ('testAddMilageBike', '000000', '2013-01-01', null, current_timestamp());

insert into milages(recorded_on, amount, created_at, bike_id) select '2013-01-01', 10, current_timestamp(), id from bikes where name = 'bike1';
insert into milages(recorded_on, amount, created_at, bike_id) select '2013-02-01', 30, current_timestamp(), id from bikes where name = 'bike1';
insert into milages(recorded_on, amount, created_at, bike_id) select '2013-03-01', 50, current_timestamp(), id from bikes where name = 'bike1';

insert into milages(recorded_on, amount, created_at, bike_id) select '2012-01-01', 10, current_timestamp(), id from bikes where name = 'bike2';

insert into assorted_trips(covered_on, distance) values ('2009-12-04', 14.2);
insert into assorted_trips(covered_on, distance) values ('2013-06-05', 21.9);
insert into assorted_trips(covered_on, distance) values ('1938-09-15', 7.7);

insert into tracks(id, covered_on, description, name, type, minlat, minlon, maxlat, maxlon) values(23, '2011-03-04', 'blah', 'RR bis Simmerath', 'biking', 1, 2, 3, 4);

insert into biking_pictures(external_id, pub_date, link) values(1, '2001-09-21 14:13:00', 'http://simons.ac');
insert into biking_pictures(external_id, pub_date, link) values(2, '2003-09-21 14:13:00', 'http://planet-punk.de');