-- =============================================
-- V11__maintenance_and_logs.sql
-- 设备维保工单 + 运行日志
-- =============================================

-- 维保工单表
CREATE TABLE maintenance_orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipment_id    UUID NOT NULL REFERENCES equipment(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    type            VARCHAR(30) NOT NULL DEFAULT 'routine'
                    CHECK (type IN ('routine', 'repair', 'inspection')),
    priority        VARCHAR(20) NOT NULL DEFAULT 'medium'
                    CHECK (priority IN ('low', 'medium', 'high', 'urgent')),
    status          VARCHAR(20) NOT NULL DEFAULT 'pending'
                    CHECK (status IN ('pending', 'in_progress', 'completed', 'cancelled')),
    assignee_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    creator_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    description     TEXT,
    resolution      TEXT,
    scheduled_date  DATE,
    completed_date  DATE,
    cost            NUMERIC(10,2),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mo_equipment ON maintenance_orders(equipment_id);
CREATE INDEX idx_mo_status ON maintenance_orders(status);
CREATE INDEX idx_mo_scheduled ON maintenance_orders(scheduled_date);
CREATE INDEX idx_mo_assignee ON maintenance_orders(assignee_id);

-- 设备运行日志表
CREATE TABLE equipment_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipment_id    UUID NOT NULL REFERENCES equipment(id) ON DELETE CASCADE,
    log_type        VARCHAR(30) NOT NULL
                    CHECK (log_type IN ('usage', 'anomaly', 'maintenance', 'sensor')),
    description     TEXT NOT NULL,
    recorded_by     UUID REFERENCES users(id) ON DELETE SET NULL,
    anomaly_flag    BOOLEAN NOT NULL DEFAULT FALSE,
    sensor_data     JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_el_equipment ON equipment_logs(equipment_id);
CREATE INDEX idx_el_type ON equipment_logs(log_type);
CREATE INDEX idx_el_created ON equipment_logs(created_at);
