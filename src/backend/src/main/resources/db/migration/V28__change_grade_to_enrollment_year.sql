-- Change tb_assessment_time.grade semantics from grade sequence (1/2/3) to enrollment year (e.g. 2024)
-- Since this is development stage, existing data with grade=1/2/3 is not meaningful.
-- Delete existing assessment data that references the old grade values.

-- Delete dependent records first (no foreign keys, but logical dependencies)
DELETE FROM tb_assessment_answer
WHERE question_id IN (
    SELECT q.id FROM tb_assessment_question q
    JOIN tb_assessment_time at ON q.assessment_time_id = at.id
);

DELETE FROM tb_assessment_question
WHERE assessment_time_id IN (SELECT id FROM tb_assessment_time);

DELETE FROM tb_assessment_time;

-- Update column comment to reflect new semantics
COMMENT ON COLUMN tb_assessment_time.grade IS '入学年份（如 2024、2025）';
