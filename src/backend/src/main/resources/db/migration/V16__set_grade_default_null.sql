-- 设置 grade 默认值为 NULL，修复 MyBatis-Plus 省略 null 字段时误用 DEFAULT 1 的问题
ALTER TABLE tb_assessment_time ALTER COLUMN grade SET DEFAULT NULL;
