ALTER TABLE tb_bug_report
    ADD COLUMN github_issue_url VARCHAR(2048),
    ADD COLUMN github_issue_number INT;

COMMENT ON COLUMN tb_bug_report.github_issue_url IS '对应 GitHub Issue 的 URL';
COMMENT ON COLUMN tb_bug_report.github_issue_number IS '对应 GitHub Issue 的编号';
