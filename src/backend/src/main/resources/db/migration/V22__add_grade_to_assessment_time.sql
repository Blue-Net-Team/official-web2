-- Add grade column to tb_assessment_time (1=大一, 2=大二, 3=大三)
ALTER TABLE tb_assessment_time ADD COLUMN grade INTEGER NOT NULL DEFAULT 1;

COMMENT ON COLUMN tb_assessment_time.grade IS '年级：1=大一, 2=大二, 3=大三';

-- Add unique constraint on (direction, epoch, grade)
ALTER TABLE tb_assessment_time ADD CONSTRAINT uk_assessment_time_direction_epoch_grade
    UNIQUE (direction, epoch, grade);
