-- ============================================
-- V26: 学习步骤 video_url 更名为 related_url（语义扩展为"相关链接"）
-- 并将种子标题同步为公开展示文案（前台切换为后端数据驱动）
-- ============================================

ALTER TABLE tb_direction_learning_step RENAME COLUMN video_url TO related_url;

COMMENT ON COLUMN tb_direction_learning_step.related_url IS '相关链接URL';

-- 同步标题为现行公开展示文案
UPDATE tb_direction_learning_step SET title = 'Python基础' WHERE direction = 'computer_vision' AND step_number = 1;
UPDATE tb_direction_learning_step SET title = 'OpenCV图像处理基础' WHERE direction = 'computer_vision' AND step_number = 2;
UPDATE tb_direction_learning_step SET title = 'Linux开发板的使用' WHERE direction = 'computer_vision' AND step_number = 3;
UPDATE tb_direction_learning_step SET title = '深度学习与目标检测' WHERE direction = 'computer_vision' AND step_number = 4;

UPDATE tb_direction_learning_step SET title = 'C语言基础' WHERE direction = 'embedded' AND step_number = 1;
UPDATE tb_direction_learning_step SET title = '单片机基础' WHERE direction = 'embedded' AND step_number = 2;
UPDATE tb_direction_learning_step SET title = '外设通信与控制' WHERE direction = 'embedded' AND step_number = 3;
UPDATE tb_direction_learning_step SET title = 'PCB设计与绘制' WHERE direction = 'embedded' AND step_number = 4;

UPDATE tb_direction_learning_step SET title = '机械制图基础' WHERE direction = 'structural_design' AND step_number = 1;
UPDATE tb_direction_learning_step SET title = '三维建模入门' WHERE direction = 'structural_design' AND step_number = 2;
UPDATE tb_direction_learning_step SET title = '装配与工程图' WHERE direction = 'structural_design' AND step_number = 3;
UPDATE tb_direction_learning_step SET title = '仿真与优化' WHERE direction = 'structural_design' AND step_number = 4;
