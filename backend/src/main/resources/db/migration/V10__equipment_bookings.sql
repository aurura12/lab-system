-- =============================================
-- V10__equipment_bookings.sql
-- 设备预约表
-- =============================================

CREATE TABLE equipment_bookings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipment_id    UUID NOT NULL REFERENCES equipment(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id      UUID REFERENCES projects(id) ON DELETE SET NULL,
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending'
                    CHECK (status IN ('pending','approved','rejected','checked_in','no_show','completed','cancelled')),
    purpose         TEXT,
    experiment_type VARCHAR(100),
    checkin_time    TIMESTAMP,
    approval_note   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(equipment_id, start_time)
);

CREATE INDEX idx_booking_equipment_time ON equipment_bookings(equipment_id, start_time);
CREATE INDEX idx_booking_user ON equipment_bookings(user_id);
CREATE INDEX idx_booking_status ON equipment_bookings(status);
CREATE INDEX idx_booking_time ON equipment_bookings(start_time, end_time);
