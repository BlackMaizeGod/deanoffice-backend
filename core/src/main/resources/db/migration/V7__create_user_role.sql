create table user_role
(
	id serial not null,
	role varchar(56),
	app_user_id integer
)
;

INSERT INTO user_role (id, role, app_user_id) VALUES (1, 'ADMIN', 3);