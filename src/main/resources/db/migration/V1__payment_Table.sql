create table payment
(id integer generated by default as identity,
 amount integer not null,
 bene_name varchar(255),
 bene_acc_num bigint,
 bene_ifsc varchar(255),
 payee_name varchar(255),
 payee_acc_num bigint,
 payee_ifsc varchar(255),
 status varchar(25),
 request_id varchar(50),
 primary key (id))
