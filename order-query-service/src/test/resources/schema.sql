DROP TABLE IF EXISTS orders;

CREATE TABLE orders (ID INT PRIMARY KEY, stateId varchar(30), flowId varchar(30),
      amount decimal, order_date timestamp with time zone,
      sla_yellow_date timestamp with time zone,
      sla_red_date timestamp with time zone
  );
--- Data notes. IDs are numeric and range from 1 to 30. All order amounts are 2000 + Id - 1
--- All order_dates range from 2024-05-01 to 2024-05-30.
--- All sla_yellow_dates are order dates + 2 days. All sla_red_dates are sla_yellow_date + 2 days.
--- The exception is for ID 16 so that we can demonstrate the toDoList features.
--- CREATED state for IDs 1 - 15 , PAYMENT_INITIATED for IDs 16-20, PAYMENT_CONFIRMED for IDs 21-25
--- and CLOSED for IDs 26 - 30
--- These conventions keep the results predictable and hence - assertable.
INSERT INTO orders (ID,stateId, flowId, amount,order_date,sla_yellow_date,sla_red_date) VALUES (1,'CREATED','order-flow',2000, '2024-05-01 12:00:00+5:30','2024-05-03 12:00:00+5:30','2024-05-05 12:00:00+5:30');
INSERT INTO orders (ID,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) VALUES (2,'CREATED','order-flow',2001, '2024-05-02 12:00:00+5:30','2024-05-04 12:00:00+5:30','2024-05-06 12:00:00+5:30');
INSERT INTO orders (ID,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) VALUES (3,'CREATED','order-flow',2002, '2024-05-03 12:00:00+5:30','2024-05-05 12:00:00+5:30','2024-05-07 12:00:00+5:30');
INSERT INTO orders (ID,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) VALUES (4,'CREATED','order-flow',2003, '2024-05-04 12:00:00+5:30','2024-05-06 12:00:00+5:30','2024-05-08 12:00:00+5:30');
INSERT INTO orders (ID,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) VALUES (5,'CREATED','order-flow',2004, '2024-05-05 12:00:00+5:30','2024-05-07 12:00:00+5:30','2024-05-09 12:00:00+5:30');
INSERT INTO orders (ID,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) VALUES (6,'CREATED','order-flow',2005, '2024-05-06 12:00:00+5:30','2024-05-08 12:00:00+5:30','2024-05-10 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (7,'CREATED','order-flow', 2006, '2024-05-07 12:00:00+5:30','2024-05-09 12:00:00+5:30','2024-05-11 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (8,'CREATED','order-flow', 2007, '2024-05-08 12:00:00+5:30','2024-05-10 12:00:00+5:30','2024-05-12 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (9, 'CREATED','order-flow',2008, '2024-05-09 12:00:00+5:30','2024-05-11 12:00:00+5:30','2024-05-13 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (10,'CREATED','order-flow', 2009, '2024-05-10 12:00:00+5:30','2024-05-12 12:00:00+5:30','2024-05-14 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (11,'CREATED','order-flow', 2010, '2024-05-11 12:00:00+5:30','2024-05-13 12:00:00+5:30','2024-05-15 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (12,'CREATED','order-flow', 2011, '2024-05-12 12:00:00+5:30','2024-05-14 12:00:00+5:30','2024-05-16 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (13,'CREATED','order-flow', 2012, '2024-05-13 12:00:00+5:30','2024-05-15 12:00:00+5:30','2024-05-17 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (14, 'CREATED','order-flow',2013, '2024-05-14 12:00:00+5:30','2024-05-16 12:00:00+5:30','2024-05-18 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (15, 'CREATED','order-flow',2014, '2024-05-15 12:00:00+5:30','2024-05-17 12:00:00+5:30','2024-05-19 12:00:00+5:30');
-- the data below is aberrant and was created to test out the toDoList functionality
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (16, 'PAYMENT_INITIATED','order-flow',2015, '2024-05-16 12:00:00+5:30','2024-05-20 12:00:00+5:30','2024-05-24 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (17, 'PAYMENT_INITIATED','order-flow',2016, '2024-05-17 12:00:00+5:30','2024-05-19 12:00:00+5:30','2024-05-21 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (18, 'PAYMENT_INITIATED','order-flow',2017, '2024-05-18 12:00:00+5:30','2024-05-20 12:00:00+5:30','2024-05-22 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (19, 'PAYMENT_INITIATED','order-flow',2018, '2024-05-19 12:00:00+5:30','2024-05-21 12:00:00+5:30','2024-05-23 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (20, 'PAYMENT_INITIATED','order-flow',2019, '2024-05-20 12:00:00+5:30','2024-05-22 12:00:00+5:30','2024-05-24 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (21, 'PAYMENT_CONFIRMED','order-flow',2020, '2024-05-21 12:00:00+5:30','2024-05-23 12:00:00+5:30','2024-05-25 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (22, 'PAYMENT_CONFIRMED','order-flow',2021, '2024-05-22 12:00:00+5:30','2024-05-24 12:00:00+5:30','2024-05-26 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (23, 'PAYMENT_CONFIRMED','order-flow',2022, '2024-05-23 12:00:00+5:30','2024-05-25 12:00:00+5:30','2024-05-27 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (24, 'PAYMENT_CONFIRMED','order-flow',2023, '2024-05-24 12:00:00+5:30','2024-05-26 12:00:00+5:30','2024-05-28 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (25, 'PAYMENT_CONFIRMED','order-flow',2024, '2024-05-25 12:00:00+5:30','2024-05-27 12:00:00+5:30','2024-05-29 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (26, 'CLOSED','order-flow',2025, '2024-05-26 12:00:00+5:30','2024-05-28 12:00:00+5:30','2024-05-30 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (27, 'CLOSED','order-flow',2026, '2024-05-27 12:00:00+5:30','2024-05-29 12:00:00+5:30','2024-05-31 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (28, 'CLOSED','order-flow',2027, '2024-05-28 12:00:00+5:30','2024-05-30 12:00:00+5:30','2024-06-01 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (29, 'CLOSED','order-flow',2028, '2024-05-29 12:00:00+5:30','2024-05-31 12:00:00+5:30','2024-06-02 12:00:00+5:30');
insert into orders(id,stateId, flowId,amount,order_date,sla_yellow_date,sla_red_date) values (30, 'CLOSED','order-flow',2029, '2024-05-30 12:00:00+5:30','2024-06-01 12:00:00+5:30','2024-06-03 12:00:00+5:30');

DROP TABLE IF EXISTS order_lines;

CREATE TABLE order_lines (ID INT PRIMARY KEY, product varchar(32), order_id int,
           quantity int, unit_price decimal);
insert into order_lines(id, product,order_id, quantity, unit_price) values(1,'prod11',1,5,200);
insert into order_lines(id, product,order_id, quantity, unit_price) values(2,'prod12',1,10,100);
insert into order_lines(id, product,order_id, quantity, unit_price) values(3,'prod21',2,5,200);
insert into order_lines(id, product,order_id, quantity, unit_price) values(4,'prod22',2,10,100);
insert into order_lines(id, product,order_id, quantity, unit_price) values(5,'prod23',2,1,1);

insert into order_lines(id,product,order_id, quantity, unit_price) values(6,'prod31', 3,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(7,'prod32', 3,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(8,'prod33', 3,1,2);

insert into order_lines(id,product,order_id, quantity, unit_price) values(9,'prod41', 4,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(10,'prod42', 4,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(11,'prod43', 4,1,3);

insert into order_lines(id,product,order_id, quantity, unit_price) values(12,'prod51', 5,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(13,'prod52', 5,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(14,'prod53', 5,1,4);

insert into order_lines(id,product,order_id, quantity, unit_price) values(15,'prod61', 6,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(16,'prod62', 6,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(17,'prod63', 6,1,5);

insert into order_lines(id,product,order_id, quantity, unit_price) values(18,'prod71', 7,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(19,'prod72', 7,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(20,'prod73', 7,1,6);

insert into order_lines(id,product,order_id, quantity, unit_price) values(21,'prod81', 8,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(22,'prod82', 8,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(23,'prod83', 8,1,7);

insert into order_lines(id,product,order_id, quantity, unit_price) values(24,'prod91', 9,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(25,'prod92', 9,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(26,'prod93', 9,1,8);

insert into order_lines(id,product,order_id, quantity, unit_price) values(27,'prod101', 10,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(28,'prod102', 10,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(29,'prod103', 10,1,9);

insert into order_lines(id,product,order_id, quantity, unit_price) values(30,'prod111', 11,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(31,'prod112', 11,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(32,'prod113', 11,1,10);

insert into order_lines(id,product,order_id, quantity, unit_price) values(33,'prod121', 12,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(34,'prod122', 12,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(35,'prod123', 12,1,11);

insert into order_lines(id,product,order_id, quantity, unit_price) values(36,'prod131', 13,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(37,'prod132', 13,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(38,'prod133', 13,1,12);

insert into order_lines(id,product,order_id, quantity, unit_price) values(39,'prod141', 14,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(40,'prod142', 14,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(41,'prod143', 14,1,13);

insert into order_lines(id,product,order_id, quantity, unit_price) values(42,'prod151', 15,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(43,'prod152', 15,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(44,'prod153', 15,1,14);

insert into order_lines(id,product,order_id, quantity, unit_price) values(45,'prod161', 16,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(46,'prod162', 16,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(47,'prod163', 16,1,15);

insert into order_lines(id,product,order_id, quantity, unit_price) values(48,'prod171', 17,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(49,'prod172', 17,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(50,'prod173', 17,1,16);

insert into order_lines(id,product,order_id, quantity, unit_price) values(51,'prod181', 18,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(52,'prod182', 18,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(53,'prod183', 18,1,17);

insert into order_lines(id,product,order_id, quantity, unit_price) values(54,'prod191', 19,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(55,'prod192', 19,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(56,'prod193', 19,1,18);

insert into order_lines(id,product,order_id, quantity, unit_price) values(57,'prod201', 20,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(58,'prod202', 20,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(59,'prod203', 20,1,19);

insert into order_lines(id,product,order_id, quantity, unit_price) values(60,'prod211', 21,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(61,'prod212', 21,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(62,'prod213', 21,1,20);

insert into order_lines(id,product,order_id, quantity, unit_price) values(63,'prod221', 22,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(64,'prod222', 22,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(65,'prod223', 22,1,21);

insert into order_lines(id,product,order_id, quantity, unit_price) values(66,'prod231', 23,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(67,'prod232', 23,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(68,'prod233', 23,1,22);

insert into order_lines(id,product,order_id, quantity, unit_price) values(69,'prod241', 24,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(70,'prod242', 24,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(71,'prod243', 24,1,23);

insert into order_lines(id,product,order_id, quantity, unit_price) values(72,'prod251', 25,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(73,'prod252', 25,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(74,'prod253', 25,1,24);

insert into order_lines(id,product,order_id, quantity, unit_price) values(75,'prod261', 26,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(76,'prod262', 26,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(77,'prod263', 26,1,25);

insert into order_lines(id,product,order_id, quantity, unit_price) values(78,'prod271', 27,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(79,'prod272', 27,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(80,'prod273', 27,1,26);

insert into order_lines(id,product,order_id, quantity, unit_price) values(81,'prod281', 28,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(82,'prod282', 28,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(83,'prod283', 28,1,27);

insert into order_lines(id,product,order_id, quantity, unit_price) values(84,'prod291', 29,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(85,'prod292', 29,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(86,'prod293', 29,1,28);

insert into order_lines(id,product,order_id, quantity, unit_price) values(87,'prod301', 30,5,200);
insert into order_lines(id,product,order_id, quantity, unit_price) values(88,'prod302', 30,10,100);
insert into order_lines(id,product,order_id, quantity, unit_price) values(89,'prod303', 30,1,29);
