-- 考核时间增加结果发布时间标记
ALTER TABLE tb_assessment_time ADD COLUMN results_published_at TIMESTAMP NULL;

-- 评论表增加用户-答案唯一约束，防止同一用户重复评论
ALTER TABLE tb_comment ADD CONSTRAINT uk_comment_answer_user UNIQUE (answer_id, user_id);
