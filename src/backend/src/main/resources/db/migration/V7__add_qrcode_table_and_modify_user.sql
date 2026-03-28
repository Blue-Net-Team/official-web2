-- 创建二维码表并修改用户表关联关系

-- 1. 创建二维码表
CREATE TABLE tb_qrcode (
    id SERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL,
    type VARCHAR(50)
);

-- 2. 修改用户表：将 wechat_qrcode 改为 qrcode_id
ALTER TABLE tb_user
    DROP COLUMN wechat_qrcode,
    ADD COLUMN qrcode_id BIGINT;

-- 3. 添加注释
COMMENT ON TABLE tb_qrcode IS '二维码表';
COMMENT ON COLUMN tb_qrcode.id IS '二维码ID';
COMMENT ON COLUMN tb_qrcode.file_id IS '关联的文件ID';
COMMENT ON COLUMN tb_qrcode.type IS '二维码类型';
COMMENT ON COLUMN tb_user.qrcode_id IS '关联的微信二维码ID';
