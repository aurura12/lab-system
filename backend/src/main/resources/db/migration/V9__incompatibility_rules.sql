-- =============================================
-- V9__incompatibility_rules.sql
-- 试剂配伍禁忌规则表
-- =============================================

CREATE TABLE incompatibility_rules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_a_id   UUID NOT NULL REFERENCES reagent_categories(id) ON DELETE CASCADE,
    category_b_id   UUID NOT NULL REFERENCES reagent_categories(id) ON DELETE CASCADE,
    hazard_type     VARCHAR(50) NOT NULL CHECK (hazard_type IN ('fire', 'explosion', 'toxic_gas', 'corrosion', 'other')),
    severity        VARCHAR(20) NOT NULL CHECK (severity IN ('warning', 'danger', 'critical')),
    description     TEXT NOT NULL,
    action_required TEXT,
    scenario        VARCHAR(50) NOT NULL DEFAULT 'storage' CHECK (scenario IN ('storage', 'usage', 'disposal', 'all')),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(category_a_id, category_b_id)
);

CREATE INDEX idx_rule_category_a ON incompatibility_rules(category_a_id);
CREATE INDEX idx_rule_category_b ON incompatibility_rules(category_b_id);
CREATE INDEX idx_rule_scenario ON incompatibility_rules(scenario);

-- =============================================
-- 常用禁忌规则种子数据
-- =============================================
INSERT INTO incompatibility_rules (category_a_id, category_b_id, hazard_type, severity, description, action_required, scenario)
SELECT a.id, b.id, 'explosion', 'critical',
       '酸性物质与氰化物混合会释放剧毒氰化氢气体',
       '严禁混放，分柜储存', 'storage'
FROM reagent_categories a, reagent_categories b
WHERE a.id <> b.id AND a.hazard_class = 'corrosive' AND b.name LIKE '%氰%'
ON CONFLICT DO NOTHING;

INSERT INTO incompatibility_rules (category_a_id, category_b_id, hazard_type, severity, description, action_required, scenario)
SELECT a.id, b.id, 'fire', 'critical',
       '氧化剂与还原剂混合可能引发剧烈氧化还原反应，导致火灾或爆炸',
       '分柜储存，严格隔离', 'storage'
FROM reagent_categories a, reagent_categories b
WHERE a.id <> b.id AND a.hazard_class = 'oxidizing' AND b.hazard_class = 'flammable'
ON CONFLICT DO NOTHING;

INSERT INTO incompatibility_rules (category_a_id, category_b_id, hazard_type, severity, description, action_required, scenario)
SELECT a.id, b.id, 'explosion', 'critical',
       '强酸与强碱混合发生剧烈中和反应，释放大量热量',
       '分柜储存，操作时佩戴防护用具', 'all'
FROM reagent_categories a, reagent_categories b
WHERE a.id <> b.id AND a.hazard_class = 'corrosive' AND b.name LIKE '%氢氧化%'
ON CONFLICT DO NOTHING;

INSERT INTO incompatibility_rules (category_a_id, category_b_id, hazard_type, severity, description, action_required, scenario)
SELECT a.id, b.id, 'explosion', 'danger',
       '高锰酸钾与甘油等有机物混合可能发生爆炸',
       '严禁混放，远离有机物', 'storage'
FROM reagent_categories a, reagent_categories b
WHERE a.id <> b.id AND a.name LIKE '%高锰酸钾%' AND b.name LIKE '%甘油%'
ON CONFLICT DO NOTHING;

INSERT INTO incompatibility_rules (category_a_id, category_b_id, hazard_type, severity, description, action_required, scenario)
SELECT a.id, b.id, 'toxic_gas', 'critical',
       '含氯溶剂与强酸混合可能释放有毒氯气',
       '分柜储存，通风橱内操作', 'all'
FROM reagent_categories a, reagent_categories b
WHERE a.id <> b.id AND a.hazard_class = 'corrosive' AND b.name LIKE '%氯%'
ON CONFLICT DO NOTHING;

INSERT INTO incompatibility_rules (category_a_id, category_b_id, hazard_type, severity, description, action_required, scenario)
SELECT a.id, b.id, 'explosion', 'danger',
       '过氧化氢与有机物或金属粉末混合可能分解爆炸',
       '分柜储存，避免与有机物接触', 'storage'
FROM reagent_categories a, reagent_categories b
WHERE a.id <> b.id AND a.name LIKE '%过氧化氢%' AND b.name NOT LIKE '%过氧化氢%'
ON CONFLICT DO NOTHING;
