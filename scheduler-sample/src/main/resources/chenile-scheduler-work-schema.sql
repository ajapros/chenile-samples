create table if not exists chenile_scheduler_work_item (
	id varchar(128) primary key,
	job_name varchar(200) not null,
	scheduled_fire_time timestamp not null,
	idempotency_key varchar(256) not null,
	payload text,
	status varchar(32) not null,
	attempt integer not null default 0,
	locked_by varchar(200),
	locked_until timestamp,
	created_at timestamp not null,
	updated_at timestamp not null,
	finished_at timestamp,
	error_message varchar(4000),
	constraint uk_chenile_scheduler_work_idempotency unique (idempotency_key)
);

create index if not exists idx_chenile_scheduler_work_backlog
	on chenile_scheduler_work_item (status, locked_until);
