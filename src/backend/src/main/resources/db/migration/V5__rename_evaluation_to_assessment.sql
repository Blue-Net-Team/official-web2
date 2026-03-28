-- 将考核相关表、列、索引、权限从 evaluation 重命名为 assessment（术语统一）

-- 1. 重命名表
ALTER TABLE tb_evaluation_time RENAME TO tb_assessment_time;
ALTER TABLE tb_evaluation_question RENAME TO tb_assessment_question;
ALTER TABLE tb_evaluation_answer RENAME TO tb_assessment_answer;

-- 2. 重命名列（仅 tb_assessment_question 有 evaluation_time_id）
ALTER TABLE tb_assessment_question RENAME COLUMN evaluation_time_id TO assessment_time_id;

-- 3. 重命名索引
ALTER INDEX idx_eval_time_direction RENAME TO idx_asm_time_direction;
ALTER INDEX idx_eval_time_epoch RENAME TO idx_asm_time_epoch;
ALTER INDEX idx_eval_question_time_id RENAME TO idx_asm_question_time_id;
ALTER INDEX idx_eval_question_type RENAME TO idx_asm_question_type;
ALTER INDEX idx_eval_answer_user_id RENAME TO idx_asm_answer_user_id;
ALTER INDEX idx_eval_answer_question_id RENAME TO idx_asm_answer_question_id;

-- 4. 更新表与列注释（tb_file.type、tb_comment.answer_id 及考核表）
COMMENT ON TABLE tb_assessment_time IS '考核时间表，定义各方向各轮次的考核时间范围和限时规则';
COMMENT ON COLUMN tb_assessment_time.id IS '考核时间ID，主键，自增';
COMMENT ON COLUMN tb_assessment_time.direction IS '考核方向，枚举值：计算机视觉、结构设计、嵌入式开发';
COMMENT ON COLUMN tb_assessment_time.epoch IS '考核轮次，0表示最终考核，1、2、3...表示第1、2、3...轮考核';
COMMENT ON COLUMN tb_assessment_time.start_time IS '考核开始时间';
COMMENT ON COLUMN tb_assessment_time.end_time IS '考核结束时间';
COMMENT ON COLUMN tb_assessment_time.time_limit IS '是否限时，true表示限时考核';
COMMENT ON COLUMN tb_assessment_time.time_limit_minutes IS '限时分钟数，time_limit为true时有效';

COMMENT ON TABLE tb_assessment_question IS '考核题目表，存储各轮次考核的具体题目';
COMMENT ON COLUMN tb_assessment_question.id IS '题目ID，主键，自增';
COMMENT ON COLUMN tb_assessment_question.assessment_time_id IS '所属考核时间ID，关联 tb_assessment_time.id';
COMMENT ON COLUMN tb_assessment_question.question_no IS '题目序号，在同一考核时间下唯一';
COMMENT ON COLUMN tb_assessment_question.question_type IS '题目类型，枚举值：SINGLE_CHOICE(单选), MULTIPLE_CHOICE(多选), FILE_UPLOAD(文件上传), ALGORITHM(算法题)';
COMMENT ON COLUMN tb_assessment_question.title IS '考题标题';
COMMENT ON COLUMN tb_assessment_question.content IS '题目内容，JSON格式，包含选项、正确答案、算法用例等';
COMMENT ON COLUMN tb_assessment_question.attachment_id IS '附件文件ID，关联 tb_file.id，用于上传题附件';
COMMENT ON COLUMN tb_assessment_question.score IS '题目满分分值';

COMMENT ON TABLE tb_assessment_answer IS '考核答案表，存储考生的答题内容和评分';
COMMENT ON COLUMN tb_assessment_answer.id IS '答案ID，主键，自增';
COMMENT ON COLUMN tb_assessment_answer.user_id IS '考生用户ID，关联 tb_user.id';
COMMENT ON COLUMN tb_assessment_answer.question_id IS '题目ID，关联 tb_assessment_question.id';
COMMENT ON COLUMN tb_assessment_answer.content IS '答案内容，文本答案或算法代码';
COMMENT ON COLUMN tb_assessment_answer.language IS '编程语言，算法题时使用，如 python, c, cpp, java, js';
COMMENT ON COLUMN tb_assessment_answer.file_id IS '文件答案ID，关联 tb_file.id，用于文件上传题';
COMMENT ON COLUMN tb_assessment_answer.submit_time IS '提交时间，默认为当前时间';

COMMENT ON COLUMN tb_file.type IS '文件类型，枚举值：avatar(头像), normal_img(普通图片), assessment_attachment(考题附件), work(考生作品), qrcode(二维码)';
COMMENT ON COLUMN tb_comment.answer_id IS '关联的答案ID，关联 tb_assessment_answer.id';

COMMENT ON INDEX idx_asm_time_direction IS '优化按方向查询考核时间的性能';
COMMENT ON INDEX idx_asm_time_epoch IS '优化按轮次查询考核时间的性能';
COMMENT ON INDEX idx_asm_question_time_id IS '优化查询指定考核时间下所有题目的性能';
COMMENT ON INDEX idx_asm_question_type IS '优化按题目类型查询的性能';
COMMENT ON INDEX idx_asm_answer_user_id IS '优化按用户查询答案的性能';
COMMENT ON INDEX idx_asm_answer_question_id IS '优化按题目查询答案的性能';
