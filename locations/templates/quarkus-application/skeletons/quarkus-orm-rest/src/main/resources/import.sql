-- This file allow to write SQL commands that will be emitted in test and dev.
insert into myentity (id, field) values(1, 'field-1');
insert into myentity (id, field) values(2, 'field-2');
insert into myentity (id, field) values(3, 'field-3');
alter sequence myentity_seq restart with 4;