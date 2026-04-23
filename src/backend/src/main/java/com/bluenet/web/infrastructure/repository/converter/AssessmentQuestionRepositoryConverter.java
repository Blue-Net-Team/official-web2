package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentQuestionDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 评测题目仓储转换器。
 * <p>
 * 负责 AssessmentQuestion 的 DO 与 Entity 之间的显式字段映射。
 * </p>
 */
@Component
public class AssessmentQuestionRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）。
     *
     * @param entity
     *            领域实体
     * @return 数据对象
     */
    public AssessmentQuestionDO toDataObject(AssessmentQuestion entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentQuestionDO.builder()
                .id(entity.getId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .questionNo(entity.getQuestionNo())
                .questionType(entity.getQuestionType())
                .title(entity.getTitle())
                .content(entity.getContent())
                .attachmentId(entity.getAttachmentId())
                .score(entity.getScore())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）。
     *
     * @param dataObject
     *            数据对象
     * @return 领域实体
     */
    public AssessmentQuestion toEntity(AssessmentQuestionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AssessmentQuestion.reconstruct(
                dataObject.getId(),
                dataObject.getAssessmentTimeId(),
                dataObject.getQuestionNo(),
                dataObject.getQuestionType(),
                dataObject.getTitle(),
                dataObject.getContent(),
                dataObject.getAttachmentId(),
                dataObject.getScore());
    }

    /**
     * DO 列表 → Entity 列表。
     *
     * @param dataObjects
     *            数据对象列表
     * @return 领域实体列表
     */
    public List<AssessmentQuestion> toEntityList(List<AssessmentQuestionDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
