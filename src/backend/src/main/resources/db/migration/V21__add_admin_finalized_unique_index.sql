CREATE UNIQUE INDEX uk_judgement_admin_finalized
    ON tb_assessment_judgement (answer_id, source)
    WHERE source = 'ADMIN_FINALIZED';
