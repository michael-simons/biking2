
insert into bikes(name, color, decommissioned_on, created_at) values ('bike1', '000000', null, current_timestamp());
insert into bikes(name, color, decommissioned_on, created_at) values ('bike2', '000000', '2012-09-30', current_timestamp());
insert into bikes(name, color, decommissioned_on, created_at) values ('bike3', '000000', '2014-01-31', current_timestamp());
insert into bikes(name, color, decommissioned_on, created_at) values ('testAddMilageBike', '000000', null, current_timestamp());

insert into milages(recorded_on, amount, created_at, bike_id) select '2013-01-01', 10, current_timestamp(), id from bikes where name = 'bike1';
insert into milages(recorded_on, amount, created_at, bike_id) select '2013-02-01', 30, current_timestamp(), id from bikes where name = 'bike1';
insert into milages(recorded_on, amount, created_at, bike_id) select '2013-03-01', 50, current_timestamp(), id from bikes where name = 'bike1';

insert into milages(recorded_on, amount, created_at, bike_id) select '2012-01-01', 10, current_timestamp(), id from bikes where name = 'bike2';