-- 为 github_issue_number 添加唯一约束，防止并发场景下重复创建 BugReport
ALTER TABLE tb_bug_report
    ADD CONSTRAINT uk_bug_report_github_issue_number UNIQUE (github_issue_number);
