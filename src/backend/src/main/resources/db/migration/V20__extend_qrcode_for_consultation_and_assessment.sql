-- 扩展二维码表支持咨询群和考核群

-- 1. 添加新字段
ALTER TABLE tb_qrcode
    ADD COLUMN epoch INT,
    ADD COLUMN direction VARCHAR(50),
    ADD COLUMN is_shared BOOLEAN DEFAULT FALSE;

-- 2. 添加注释
COMMENT ON COLUMN tb_qrcode.epoch IS '考核轮次（仅ASSESSMENT类型使用）';
COMMENT ON COLUMN tb_qrcode.direction IS '方向（仅ASSESSMENT类型使用）';
COMMENT ON COLUMN tb_qrcode.is_shared IS '是否三方向共用（仅ASSESSMENT类型最后一轮使用）';
