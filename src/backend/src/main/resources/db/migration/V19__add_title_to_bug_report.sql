ALTER TABLE tb_bug_report
    ADD COLUMN title VARCHAR(200);

UPDATE tb_bug_report
SET title = description
WHERE title IS NULL;

ALTER TABLE tb_bug_report
    ALTER COLUMN title SET NOT NULL;

COMMENT ON COLUMN tb_bug_report.title IS 'Bug 标题（简述）';
