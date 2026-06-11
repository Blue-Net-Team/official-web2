-- 先清理重复数据：同一 answer_id 下保留 id 最大（最新）的 ADMIN_FINALIZED 记录
DELETE FROM tb_assessment_judgement t1
WHERE source = 'ADMIN_FINALIZED'
  AND EXISTS (
    SELECT 1 FROM tb_assessment_judgement t2
    WHERE t2.answer_id = t1.answer_id
      AND t2.source = t1.source
      AND t2.id > t1.id
  );

-- 创建唯一索引，确保同一 answer 只有一个 ADMIN_FINALIZED 评判
CREATE UNIQUE INDEX uk_judgement_admin_finalized
    ON tb_assessment_judgement (answer_id, source)
    WHERE source = 'ADMIN_FINALIZED';
