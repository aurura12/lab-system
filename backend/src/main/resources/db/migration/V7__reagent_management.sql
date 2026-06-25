-- =============================================
-- V7__reagent_management.sql
-- 试剂管理模块：位置体系 + 品类 + 库存 + 交易记录
-- =============================================

-- =============================================
-- 1. STORAGE_LOCATIONS — 存储位置（树形自引用）
-- 层级: Room → Cabinet → Shelf → Grid
-- 用 parent_id 自引用实现任意层级的树形结构
-- =============================================
CREATE TABLE storage_locations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id       UUID REFERENCES storage_locations(id) ON DELETE CASCADE,
    room_id         UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    level           VARCHAR(20) NOT NULL CHECK (level IN ('cabinet', 'shelf', 'grid')),
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(100),
    path            VARCHAR(200),
    description     TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(room_id, parent_id, code),
    UNIQUE(room_id, parent_id, level, code)
);

CREATE INDEX idx_location_room ON storage_locations(room_id);
CREATE INDEX idx_location_parent ON storage_locations(parent_id);

-- =============================================
-- 2. REAGENT_CATEGORIES — 试剂品类表（模板定义）
-- =============================================
CREATE TABLE reagent_categories (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                  VARCHAR(150) NOT NULL,
    cas_no                VARCHAR(50) UNIQUE,
    molecular_formula     VARCHAR(100),
    specification         VARCHAR(100),
    hazard_class          VARCHAR(50) NOT NULL DEFAULT 'none'
                          CHECK (hazard_class IN ('flammable', 'corrosive', 'oxidizing', 'toxic', 'none')),
    unit                  VARCHAR(20) NOT NULL DEFAULT '瓶',
    default_shelf_life_days INTEGER,
    min_stock_threshold   INTEGER,
    storage_conditions    TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_category_name ON reagent_categories(name);
CREATE INDEX idx_category_cas ON reagent_categories(cas_no);
CREATE INDEX idx_category_hazard ON reagent_categories(hazard_class);

-- =============================================
-- 3. REAGENT_INVENTORY — 试剂库存（每瓶/每份）
-- =============================================
CREATE TABLE reagent_inventory (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id         UUID NOT NULL REFERENCES reagent_categories(id),
    barcode             VARCHAR(100) UNIQUE,
    location_id         UUID REFERENCES storage_locations(id) ON DELETE SET NULL,
    batch_no            VARCHAR(100),
    total_quantity      NUMERIC(12,4) NOT NULL CHECK (total_quantity > 0),
    remaining_quantity  NUMERIC(12,4) NOT NULL CHECK (remaining_quantity >= 0),
    unit                VARCHAR(20) NOT NULL,
    manufacture_date    DATE,
    expiry_date         DATE NOT NULL,
    received_date       DATE NOT NULL DEFAULT CURRENT_DATE,
    supplier            VARCHAR(150),
    status              VARCHAR(20) NOT NULL DEFAULT 'unopened'
                        CHECK (status IN ('unopened', 'opened', 'expired', 'disposed')),
    storage_conditions  TEXT,
    alert_level         VARCHAR(20) NOT NULL DEFAULT 'normal'
                        CHECK (alert_level IN ('normal', 'warning_days', 'warning_urgent', 'expired')),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- FEFO 推荐索引：同一品类下按有效期升序，跳过已过期/已废弃
CREATE INDEX idx_reagent_fefo ON reagent_inventory(category_id, status, expiry_date ASC)
    WHERE status NOT IN ('expired', 'disposed');

-- 预警查询索引：只索引非正常的预警级别
CREATE INDEX idx_reagent_alert ON reagent_inventory(alert_level)
    WHERE alert_level != 'normal';

-- 条码和位置查询索引
CREATE INDEX idx_inventory_barcode ON reagent_inventory(barcode);
CREATE INDEX idx_inventory_location ON reagent_inventory(location_id);
CREATE INDEX idx_inventory_category ON reagent_inventory(category_id);
CREATE INDEX idx_inventory_expiry ON reagent_inventory(expiry_date);
CREATE INDEX idx_inventory_status ON reagent_inventory(status);

-- =============================================
-- 4. REAGENT_TRANSACTIONS — 出入库记录
-- quantity 永远为正数，由 type 决定出入方向
-- =============================================
CREATE TABLE reagent_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id    UUID NOT NULL REFERENCES reagent_inventory(id),
    type            VARCHAR(20) NOT NULL CHECK (type IN ('inbound', 'outbound')),
    quantity        NUMERIC(12,4) NOT NULL CHECK (quantity > 0),
    operator_id     UUID NOT NULL REFERENCES users(id),
    purpose         TEXT,
    project_id      UUID REFERENCES projects(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transaction_inventory ON reagent_transactions(inventory_id);
CREATE INDEX idx_transaction_operator ON reagent_transactions(operator_id);
CREATE INDEX idx_transaction_project ON reagent_transactions(project_id);
CREATE INDEX idx_transaction_time ON reagent_transactions(created_at);
CREATE INDEX idx_transaction_type ON reagent_transactions(type);

-- =============================================
-- 5. 效期预警：更新 reagent_inventory.alert_level
-- 的函数和定时任务
-- =============================================
CREATE OR REPLACE FUNCTION update_reagent_alert_levels()
RETURNS void AS $$
BEGIN
    UPDATE reagent_inventory
    SET alert_level = CASE
        WHEN status IN ('expired', 'disposed') THEN 'expired'
        WHEN expiry_date < CURRENT_DATE THEN 'expired'
        WHEN expiry_date <= CURRENT_DATE + INTERVAL '30 days' THEN 'warning_urgent'
        WHEN expiry_date <= CURRENT_DATE + INTERVAL '90 days' THEN 'warning_days'
        ELSE 'normal'
    END,
    updated_at = NOW()
    WHERE status NOT IN ('disposed');
END;
$$ LANGUAGE plpgsql;
