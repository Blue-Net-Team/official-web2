-- 方向学习路径管理功能数据库迁移脚本
-- 创建方向学习步骤表，并初始化三个方向的默认学习路径数据

-- 1. 创建方向学习步骤表
CREATE TABLE tb_direction_learning_step (
    id BIGSERIAL PRIMARY KEY,
    direction VARCHAR(50) NOT NULL,
    step_number INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    video_url VARCHAR(500),
    CONSTRAINT uk_direction_step UNIQUE (direction, step_number)
);

COMMENT ON TABLE tb_direction_learning_step IS '方向学习步骤表，存储各方向的学习路径步骤信息';
COMMENT ON COLUMN tb_direction_learning_step.id IS '步骤ID';
COMMENT ON COLUMN tb_direction_learning_step.direction IS '方向，枚举值：computer_vision、embedded、structural_design';
COMMENT ON COLUMN tb_direction_learning_step.step_number IS '步骤序号，同一方向内唯一';
COMMENT ON COLUMN tb_direction_learning_step.title IS '步骤标题';
COMMENT ON COLUMN tb_direction_learning_step.video_url IS '视频链接URL';

-- 2. 创建索引
CREATE INDEX idx_direction_learning_step_direction ON tb_direction_learning_step(direction);

-- 3. 插入计算机视觉方向的默认学习步骤
INSERT INTO tb_direction_learning_step (direction, step_number, title, video_url) VALUES
('computer_vision', 1, 'Python基础', NULL),
('computer_vision', 2, 'OpenCV入门', NULL),
('computer_vision', 3, '深度学习基础', NULL),
('computer_vision', 4, '项目实战', NULL);

-- 4. 插入嵌入式开发方向的默认学习步骤
INSERT INTO tb_direction_learning_step (direction, step_number, title, video_url) VALUES
('embedded', 1, 'C语言基础', NULL),
('embedded', 2, '单片机入门', NULL),
('embedded', 3, 'RTOS实时操作系统', NULL),
('embedded', 4, '项目实战', NULL);

-- 5. 插入结构设计方向的默认学习步骤
INSERT INTO tb_direction_learning_step (direction, step_number, title, video_url) VALUES
('structural_design', 1, 'CAD基础', NULL),
('structural_design', 2, 'SolidWorks入门', NULL),
('structural_design', 3, '仿真分析', NULL),
('structural_design', 4, '项目实战', NULL);

-- 6. 权限记录由 PermissionScanner 自动扫描入库，无需手动插入
