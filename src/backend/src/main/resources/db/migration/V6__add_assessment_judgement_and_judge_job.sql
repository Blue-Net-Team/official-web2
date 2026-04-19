CREATE TABLE tb_assessment_judgement (
    id BIGSERIAL PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    assessment_time_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score DECIMAL(10, 2),
    max_score DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    result_code VARCHAR(50),
    source VARCHAR(50) NOT NULL,
    reviewer_id BIGINT,
    reviewer_type VARCHAR(50),
    comment TEXT,
    judged_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_assessment_decision (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    assessment_time_id BIGINT NOT NULL,
    passed BOOLEAN NOT NULL,
    decided_by BIGINT NOT NULL,
    decision_comment TEXT,
    decided_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_assessment_decision_user_time UNIQUE (user_id, assessment_time_id)
);

CREATE TABLE tb_algorithm_judge_job (
    id BIGSERIAL PRIMARY KEY,
    answer_id BIGINT,
    question_id BIGINT NOT NULL,
    assessment_time_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    language VARCHAR(50) NOT NULL,
    source_code TEXT NOT NULL,
    testcase_type VARCHAR(50) NOT NULL,
    custom_input TEXT,
    status VARCHAR(50) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retry_count INTEGER NOT NULL DEFAULT 3,
    status_message TEXT,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_algorithm_judge_case_result (
    id BIGSERIAL PRIMARY KEY,
    judge_job_id BIGINT NOT NULL,
    case_no INTEGER NOT NULL,
    testcase_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    input TEXT,
    expected_output TEXT,
    actual_output TEXT,
    stdout TEXT,
    stderr TEXT,
    time_used_ms INTEGER,
    memory_used_kb INTEGER,
    message TEXT,
    visible_to_candidate BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assessment_judgement_answer_id ON tb_assessment_judgement(answer_id);
CREATE INDEX idx_assessment_judgement_question_user ON tb_assessment_judgement(question_id, user_id);
CREATE INDEX idx_assessment_judgement_time_question ON tb_assessment_judgement(assessment_time_id, question_id);
CREATE INDEX idx_assessment_judgement_result_code ON tb_assessment_judgement(result_code);
CREATE INDEX idx_assessment_decision_time_user ON tb_assessment_decision(assessment_time_id, user_id);
CREATE INDEX idx_algorithm_judge_job_status ON tb_algorithm_judge_job(status);
CREATE INDEX idx_algorithm_judge_job_answer_id ON tb_algorithm_judge_job(answer_id);
CREATE INDEX idx_algorithm_judge_job_question_user ON tb_algorithm_judge_job(question_id, user_id);
CREATE INDEX idx_algorithm_judge_case_job_id ON tb_algorithm_judge_case_result(judge_job_id);

COMMENT ON TABLE tb_assessment_judgement IS '考核题目评判结果表';
COMMENT ON COLUMN tb_assessment_judgement.answer_id IS '答案ID';
COMMENT ON COLUMN tb_assessment_judgement.question_id IS '题目ID';
COMMENT ON COLUMN tb_assessment_judgement.assessment_time_id IS '考核时间ID';
COMMENT ON COLUMN tb_assessment_judgement.user_id IS '考生用户ID';
COMMENT ON COLUMN tb_assessment_judgement.score IS '本次评判得分';
COMMENT ON COLUMN tb_assessment_judgement.max_score IS '题目满分';
COMMENT ON COLUMN tb_assessment_judgement.status IS '评判状态';
COMMENT ON COLUMN tb_assessment_judgement.result_code IS '客观题标准结果码：AC/WA/TLE/RE/CE/MLE';
COMMENT ON COLUMN tb_assessment_judgement.source IS '评判来源：AUTO/MANUAL';
COMMENT ON COLUMN tb_assessment_judgement.reviewer_id IS '人工评判人ID，系统评判为空';
COMMENT ON COLUMN tb_assessment_judgement.reviewer_type IS '评判人类型';
COMMENT ON COLUMN tb_assessment_judgement.comment IS '评判评论';
COMMENT ON COLUMN tb_assessment_judgement.judged_at IS '完成评判时间';

COMMENT ON TABLE tb_assessment_decision IS '考生考核最终通过决策表';
COMMENT ON COLUMN tb_assessment_decision.user_id IS '考生用户ID';
COMMENT ON COLUMN tb_assessment_decision.assessment_time_id IS '考核时间ID';
COMMENT ON COLUMN tb_assessment_decision.passed IS '是否通过';
COMMENT ON COLUMN tb_assessment_decision.decided_by IS '决策管理员ID';
COMMENT ON COLUMN tb_assessment_decision.decision_comment IS '决策备注';
COMMENT ON COLUMN tb_assessment_decision.decided_at IS '决策时间';

COMMENT ON TABLE tb_algorithm_judge_job IS '算法题判题任务表';
COMMENT ON COLUMN tb_algorithm_judge_job.answer_id IS '正式提交答案ID，运行任务可为空';
COMMENT ON COLUMN tb_algorithm_judge_job.question_id IS '题目ID';
COMMENT ON COLUMN tb_algorithm_judge_job.assessment_time_id IS '考核时间ID';
COMMENT ON COLUMN tb_algorithm_judge_job.user_id IS '考生用户ID';
COMMENT ON COLUMN tb_algorithm_judge_job.language IS '提交语言';
COMMENT ON COLUMN tb_algorithm_judge_job.source_code IS '源代码';
COMMENT ON COLUMN tb_algorithm_judge_job.testcase_type IS '判题用例类型：DEFAULT_RUN/CUSTOM_RUN/FORMAL';
COMMENT ON COLUMN tb_algorithm_judge_job.custom_input IS '自定义运行输入';
COMMENT ON COLUMN tb_algorithm_judge_job.status IS '判题任务状态';
COMMENT ON COLUMN tb_algorithm_judge_job.retry_count IS '已重试次数';
COMMENT ON COLUMN tb_algorithm_judge_job.max_retry_count IS '最大重试次数';
COMMENT ON COLUMN tb_algorithm_judge_job.status_message IS '任务状态说明';
COMMENT ON COLUMN tb_algorithm_judge_job.started_at IS '开始执行时间';
COMMENT ON COLUMN tb_algorithm_judge_job.finished_at IS '结束执行时间';

COMMENT ON TABLE tb_algorithm_judge_case_result IS '算法题判题用例结果表';
COMMENT ON COLUMN tb_algorithm_judge_case_result.judge_job_id IS '判题任务ID';
COMMENT ON COLUMN tb_algorithm_judge_case_result.case_no IS '用例序号';
COMMENT ON COLUMN tb_algorithm_judge_case_result.testcase_type IS '用例类型';
COMMENT ON COLUMN tb_algorithm_judge_case_result.status IS '用例结果码';
COMMENT ON COLUMN tb_algorithm_judge_case_result.input IS '输入';
COMMENT ON COLUMN tb_algorithm_judge_case_result.expected_output IS '期望输出';
COMMENT ON COLUMN tb_algorithm_judge_case_result.actual_output IS '实际输出';
COMMENT ON COLUMN tb_algorithm_judge_case_result.stdout IS '标准输出';
COMMENT ON COLUMN tb_algorithm_judge_case_result.stderr IS '标准错误';
COMMENT ON COLUMN tb_algorithm_judge_case_result.time_used_ms IS '耗时毫秒';
COMMENT ON COLUMN tb_algorithm_judge_case_result.memory_used_kb IS '内存KB';
COMMENT ON COLUMN tb_algorithm_judge_case_result.message IS '结果说明';
COMMENT ON COLUMN tb_algorithm_judge_case_result.visible_to_candidate IS '是否对考生可见';
