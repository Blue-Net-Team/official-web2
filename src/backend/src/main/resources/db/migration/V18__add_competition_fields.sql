-- 为 tb_competition 表添加新字段：级别、月份、主办单位

-- 添加竞赛级别字段
ALTER TABLE tb_competition
ADD COLUMN level VARCHAR(20) NOT NULL DEFAULT '省级';

-- 添加举办月份字段
ALTER TABLE tb_competition
ADD COLUMN month VARCHAR(10) NULL;

-- 添加主办单位字段
ALTER TABLE tb_competition
ADD COLUMN organizer VARCHAR(200) NULL;

-- 添加字段注释
COMMENT ON COLUMN tb_competition.level IS '竞赛级别，如：国家级、省级、校级等';
COMMENT ON COLUMN tb_competition.month IS '举办月份，如：1月、2月等';
COMMENT ON COLUMN tb_competition.organizer IS '主办单位';
