CREATE TABLE tb_judge_problem_config (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    generator_language VARCHAR(50) NOT NULL,
    generator_object_key TEXT NOT NULL,
    generator_object_hash VARCHAR(128) NOT NULL,
    manifest_object_key TEXT,
    manifest_object_hash VARCHAR(128),
    primary_standard_language VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    benchmark_repeat_times INTEGER NOT NULL DEFAULT 5,
    margin_multiplier DECIMAL(10, 4) NOT NULL DEFAULT 1.5000,
    min_extra_ms INTEGER NOT NULL DEFAULT 50,
    round_to_ms INTEGER NOT NULL DEFAULT 50,
    CONSTRAINT uk_judge_problem_config_question UNIQUE (question_id)
);

CREATE TABLE tb_judge_standard_solution (
    id BIGSERIAL PRIMARY KEY,
    config_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    language VARCHAR(50) NOT NULL,
    object_key TEXT NOT NULL,
    object_hash VARCHAR(128) NOT NULL,
    primary_solution BOOLEAN NOT NULL DEFAULT FALSE,
    benchmark_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    p95_time_ms INTEGER,
    max_time_ms INTEGER,
    peak_memory_kb INTEGER,
    suggested_time_limit_ms INTEGER,
    benchmark_message TEXT,
    CONSTRAINT uk_judge_standard_solution_config_language UNIQUE (config_id, language),
    CONSTRAINT fk_judge_standard_solution_config FOREIGN KEY (config_id) REFERENCES tb_judge_problem_config(id) ON DELETE CASCADE
);

CREATE TABLE tb_judge_testcase_config (
    id BIGSERIAL PRIMARY KEY,
    config_id BIGINT NOT NULL,
    case_no INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL,
    generator_args JSONB NOT NULL DEFAULT '{}'::jsonb,
    weight DECIMAL(10, 2) NOT NULL DEFAULT 1,
    hidden BOOLEAN NOT NULL DEFAULT TRUE,
    sample BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    CONSTRAINT uk_judge_testcase_config_case UNIQUE (config_id, case_no),
    CONSTRAINT fk_judge_testcase_config_config FOREIGN KEY (config_id) REFERENCES tb_judge_problem_config(id) ON DELETE CASCADE
);

CREATE TABLE tb_judge_test_case (
    id BIGSERIAL PRIMARY KEY,
    config_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    case_no INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL,
    input_object_key TEXT NOT NULL,
    input_object_hash VARCHAR(128) NOT NULL,
    output_object_key TEXT NOT NULL,
    output_object_hash VARCHAR(128) NOT NULL,
    input_size_bytes BIGINT,
    output_size_bytes BIGINT,
    weight DECIMAL(10, 2) NOT NULL DEFAULT 1,
    hidden BOOLEAN NOT NULL DEFAULT TRUE,
    sample BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_judge_test_case_config_case UNIQUE (config_id, case_no),
    CONSTRAINT fk_judge_test_case_config FOREIGN KEY (config_id) REFERENCES tb_judge_problem_config(id) ON DELETE CASCADE
);

CREATE TABLE tb_judge_language_limit (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    language VARCHAR(50) NOT NULL,
    time_limit_ms INTEGER NOT NULL,
    memory_limit_kb INTEGER NOT NULL,
    output_limit_kb INTEGER NOT NULL DEFAULT 1024,
    confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    confirmed_at TIMESTAMP,
    source_config_id BIGINT NOT NULL,
    CONSTRAINT uk_judge_language_limit_question_language UNIQUE (question_id, language),
    CONSTRAINT fk_judge_language_limit_config FOREIGN KEY (source_config_id) REFERENCES tb_judge_problem_config(id) ON DELETE CASCADE
);

CREATE INDEX idx_judge_problem_config_status ON tb_judge_problem_config(status);
CREATE INDEX idx_judge_standard_solution_question ON tb_judge_standard_solution(question_id);
CREATE INDEX idx_judge_testcase_config_config ON tb_judge_testcase_config(config_id);
CREATE INDEX idx_judge_test_case_question ON tb_judge_test_case(question_id);
CREATE INDEX idx_judge_language_limit_confirmed ON tb_judge_language_limit(question_id, language, confirmed);

COMMENT ON TABLE tb_judge_problem_config IS '算法题当前判题数据配置表';
COMMENT ON COLUMN tb_judge_problem_config.question_id IS '算法题目ID';
COMMENT ON COLUMN tb_judge_problem_config.generator_language IS '生成器代码语言';
COMMENT ON COLUMN tb_judge_problem_config.generator_object_key IS '生成器在判题 OSS bucket 中的对象键';
COMMENT ON COLUMN tb_judge_problem_config.generator_object_hash IS '生成器文件哈希';
COMMENT ON COLUMN tb_judge_problem_config.manifest_object_key IS '后端生成的 manifest 对象键';
COMMENT ON COLUMN tb_judge_problem_config.primary_standard_language IS '用于生成标准输出的主标准解语言';
COMMENT ON COLUMN tb_judge_problem_config.status IS '配置状态：DRAFT/GENERATING/GENERATED/BENCHMARKING/READY/FAILED';
COMMENT ON COLUMN tb_judge_problem_config.benchmark_repeat_times IS '标准解 benchmark 重复次数';
COMMENT ON COLUMN tb_judge_problem_config.margin_multiplier IS '建议限时倍率';
COMMENT ON COLUMN tb_judge_problem_config.min_extra_ms IS '建议限时最小额外毫秒';
COMMENT ON COLUMN tb_judge_problem_config.round_to_ms IS '建议限时向上取整粒度';

COMMENT ON TABLE tb_judge_standard_solution IS '算法题标准解文件和 benchmark 结果表';
COMMENT ON COLUMN tb_judge_standard_solution.language IS '标准解语言';
COMMENT ON COLUMN tb_judge_standard_solution.object_key IS '标准解在判题 OSS bucket 中的对象键';
COMMENT ON COLUMN tb_judge_standard_solution.primary_solution IS '是否用于生成标准输出';
COMMENT ON COLUMN tb_judge_standard_solution.benchmark_status IS 'benchmark 状态';
COMMENT ON COLUMN tb_judge_standard_solution.p95_time_ms IS '多次运行 p95 耗时毫秒';
COMMENT ON COLUMN tb_judge_standard_solution.max_time_ms IS '多次运行最大耗时毫秒';
COMMENT ON COLUMN tb_judge_standard_solution.peak_memory_kb IS '峰值内存 KB';
COMMENT ON COLUMN tb_judge_standard_solution.suggested_time_limit_ms IS '根据公式推导的建议限时毫秒';

COMMENT ON TABLE tb_judge_testcase_config IS '算法题测试用例生成配置表';
COMMENT ON COLUMN tb_judge_testcase_config.case_no IS '测试用例序号';
COMMENT ON COLUMN tb_judge_testcase_config.category IS '测试用例分类：SAMPLE/NORMAL/EDGE/EMPTY/MINIMUM/MAXIMUM/LARGE/RANDOM/WORST_CASE/SPECIAL/REGRESSION';
COMMENT ON COLUMN tb_judge_testcase_config.generator_args IS '传给生成器的结构化参数';
COMMENT ON COLUMN tb_judge_testcase_config.hidden IS '是否隐藏用例详情';
COMMENT ON COLUMN tb_judge_testcase_config.sample IS '是否作为样例用例';

COMMENT ON TABLE tb_judge_test_case IS '算法题当前生成后的测试用例索引表';
COMMENT ON COLUMN tb_judge_test_case.input_object_key IS '输入文件 .in 在判题 OSS bucket 中的对象键';
COMMENT ON COLUMN tb_judge_test_case.output_object_key IS '标准输出 .out 在判题 OSS bucket 中的对象键';

COMMENT ON TABLE tb_judge_language_limit IS '算法题每语言资源限制表';
COMMENT ON COLUMN tb_judge_language_limit.time_limit_ms IS '正式判题限时毫秒';
COMMENT ON COLUMN tb_judge_language_limit.memory_limit_kb IS '正式判题内存限制 KB';
COMMENT ON COLUMN tb_judge_language_limit.output_limit_kb IS '正式判题输出限制 KB';
COMMENT ON COLUMN tb_judge_language_limit.confirmed IS '管理员是否已确认该语言资源限制';
