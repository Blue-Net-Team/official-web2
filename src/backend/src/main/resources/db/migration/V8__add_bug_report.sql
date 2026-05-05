CREATE TABLE tb_bug_report (
    id BIGSERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    page_url VARCHAR(2048),
    environment_json TEXT,
    reporter_email VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_bug_report_image (
    id BIGSERIAL PRIMARY KEY,
    bug_report_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL
);

CREATE INDEX idx_bug_report_status ON tb_bug_report(status);
CREATE INDEX idx_bug_report_created_at ON tb_bug_report(created_at);
CREATE INDEX idx_bug_report_image_bug_report_id ON tb_bug_report_image(bug_report_id);

COMMENT ON TABLE tb_bug_report IS 'Bug 报告表';
COMMENT ON COLUMN tb_bug_report.description IS 'Bug 描述';
COMMENT ON COLUMN tb_bug_report.page_url IS '发生页面的 URL';
COMMENT ON COLUMN tb_bug_report.environment_json IS '前端环境信息 JSON';
COMMENT ON COLUMN tb_bug_report.reporter_email IS '报告者邮箱（选填）';
COMMENT ON COLUMN tb_bug_report.status IS '状态：PENDING/IN_PROGRESS/RESOLVED';

COMMENT ON TABLE tb_bug_report_image IS 'Bug 报告关联图片表';
COMMENT ON COLUMN tb_bug_report_image.bug_report_id IS '关联的 Bug 报告 ID';
COMMENT ON COLUMN tb_bug_report_image.file_id IS '关联的文件 ID';
