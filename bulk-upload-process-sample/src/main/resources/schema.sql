create table if not exists bulk_upload_file (
	id varchar(128) primary key,
	process_id varchar(128) not null,
	object_key varchar(1000) not null,
	status varchar(64) not null,
	total_rows integer not null default 0,
	success_rows integer not null default 0,
	error_rows integer not null default 0,
	result_object_key varchar(1000),
	created_at timestamp not null,
	updated_at timestamp not null
);

create table if not exists bulk_upload_chunk (
	upload_id varchar(128) not null,
	chunk_number integer not null,
	start_line integer not null,
	end_line integer not null,
	status varchar(64) not null,
	total_rows integer not null default 0,
	success_rows integer not null default 0,
	error_rows integer not null default 0,
	updated_at timestamp not null,
	primary key (upload_id, chunk_number)
);

create table if not exists bulk_upload_row_result (
	upload_id varchar(128) not null,
	chunk_number integer not null,
	line_number integer not null,
	raw_line text,
	success boolean not null,
	error_message varchar(1000),
	created_at timestamp not null,
	primary key (upload_id, line_number)
);

create index if not exists idx_bulk_upload_chunk_status
	on bulk_upload_chunk (upload_id, status);

create index if not exists idx_bulk_upload_row_result_errors
	on bulk_upload_row_result (upload_id, success);
