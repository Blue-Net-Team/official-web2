ALTER TABLE tb_user
    ADD COLUMN assessment_grade_year INTEGER;

COMMENT ON COLUMN tb_user.assessment_grade_year IS 'Assessment grade year override for assessment matching';
