create table correspondance (
    id smallint not null primary key,
    correspondance char(120)
);
insert into correspondance (id, correspondance) values (1, 'musique');
insert into correspondance (id, correspondance) values (2, 'album');
insert into correspondance (id, correspondance) values (3, 'artiste');

create table pattern (
    id serial primary key,
    text char(120),
    id_correspondance smallint
);

insert into pattern (id_correspondance, text) values (1, 'le petit bonhome en mousse');
insert into pattern (id_correspondance, text) values (1, 'never gonna give you up');
insert into pattern (id_correspondance, text) values (3, 'david guetta');