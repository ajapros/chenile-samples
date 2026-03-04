CREATE TABLE IF NOT EXISTS vehicle (
    id VARCHAR(255) PRIMARY KEY,
    ext_type VARCHAR(64),

    -- Base workflow/entity columns
    flow_id VARCHAR(128),
    state_id VARCHAR(128),
    state_entry_time TIMESTAMP,
    tenant VARCHAR(128),
    version BIGINT,

    created_by VARCHAR(255),
    created_time TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_time TIMESTAMP,

    assignee VARCHAR(255),
    assign_comment VARCHAR(1024),
    close_comment VARCHAR(1024),
    resolve_comment VARCHAR(1024),
    description VARCHAR(2048),
    opened_by VARCHAR(255),

    sla_late BIGINT,
    sla_red_date TIMESTAMP,
    sla_tending_late BIGINT,
    sla_yellow_date TIMESTAMP,

    -- Tenant0 extension columns
    tenant0_code VARCHAR(255),
    tenant0_segment VARCHAR(255),
    tenant0_priority VARCHAR(255),
    tenant0_workflow_note VARCHAR(1024),

    -- Tenant1 extension columns
    insurance_policy_number VARCHAR(255),
    fitness_expiry VARCHAR(255),
    tenant1_code VARCHAR(255),
    tenant1_segment VARCHAR(255),
    tenant1_priority VARCHAR(255),
    tenant1_workflow_note VARCHAR(1024),

    -- Shared extension column used by tenant-specific actions
    new_column VARCHAR(1024)
);
