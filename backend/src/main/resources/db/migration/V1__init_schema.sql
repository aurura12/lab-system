-- =============================================
-- V1__init_schema.sql
-- =============================================

-- 1. USERS
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    real_name       VARCHAR(100),
    email           VARCHAR(100),
    role            VARCHAR(20)  NOT NULL DEFAULT 'researcher',
    phone           VARCHAR(20),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 2. LABS
CREATE TABLE labs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    address     VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 3. FLOORS
CREATE TABLE floors (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lab_id      UUID NOT NULL REFERENCES labs(id) ON DELETE CASCADE,
    floor_number INTEGER NOT NULL,
    name        VARCHAR(100),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(lab_id, floor_number)
);

-- 4. ROOMS
CREATE TABLE rooms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    floor_id    UUID NOT NULL REFERENCES floors(id) ON DELETE CASCADE,
    room_number VARCHAR(20) NOT NULL,
    name        VARCHAR(100),
    area_sqm    NUMERIC(8,2),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(floor_id, room_number)
);

-- 5. EQUIPMENT
CREATE TABLE equipment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150) NOT NULL,
    model           VARCHAR(100),
    serial_number   VARCHAR(100) UNIQUE,
    room_id         UUID REFERENCES rooms(id) ON DELETE SET NULL,
    category        VARCHAR(50),
    status          VARCHAR(20) NOT NULL DEFAULT 'available',
    manufacturer    VARCHAR(100),
    purchase_date   DATE,
    last_maintenance DATE,
    next_maintenance DATE,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 6. PROJECTS
CREATE TABLE projects (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(50) UNIQUE,
    description TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'active',
    start_date  DATE,
    end_date    DATE,
    owner_id    UUID REFERENCES users(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 7. PROJECT_MEMBERS
CREATE TABLE project_members (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_in_project VARCHAR(20) DEFAULT 'member',
    joined_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(project_id, user_id)
);

-- 8. USAGE_RECORDS
CREATE TABLE usage_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipment_id    UUID NOT NULL REFERENCES equipment(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    project_id      UUID REFERENCES projects(id),
    login_time      TIMESTAMP NOT NULL,
    logout_time     TIMESTAMP,
    purpose         TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'active',
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 9. DATA_RECORDS
CREATE TABLE data_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipment_id    UUID NOT NULL REFERENCES equipment(id),
    usage_record_id UUID REFERENCES usage_records(id),
    project_id      UUID REFERENCES projects(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    data_type       VARCHAR(50),
    file_name       VARCHAR(255),
    file_path       VARCHAR(500),
    file_size       BIGINT,
    description     TEXT,
    metadata_json   JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- INDEXES
CREATE INDEX idx_usage_equipment    ON usage_records(equipment_id);
CREATE INDEX idx_usage_user         ON usage_records(user_id);
CREATE INDEX idx_usage_project      ON usage_records(project_id);
CREATE INDEX idx_usage_login_time   ON usage_records(login_time);
CREATE INDEX idx_data_equipment     ON data_records(equipment_id);
CREATE INDEX idx_data_project       ON data_records(project_id);
CREATE INDEX idx_data_user          ON data_records(user_id);
CREATE INDEX idx_equipment_room     ON equipment(room_id);
CREATE INDEX idx_equipment_status   ON equipment(status);
