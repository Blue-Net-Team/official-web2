package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentAnswerDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 评测答案仓储转换器。
 * <p>
 * 负责 AssessmentAnswer 的 DO 与 Entity 之间的显式字段映射。
 * </p>
 */
@Component
public class AssessmentAnswerRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）。
     *
     * @param entity
     *            领域实体
     * @return 数据对象
     */
    public AssessmentAnswerDO toDataObject(AssessmentAnswer entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentAnswerDO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .questionId(entity.getQuestionId())
                .content(entity.getContent())
                .language(entity.getLanguage())
                .fileId(entity.getFileId())
                .submitTime(entity.getSubmitTime())
                .teamId(entity.getTeamId())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）。
     *
     * @param dataObject
     *            数据对象
     * @return 领域实体
     */
    public AssessmentAnswer toEntity(AssessmentAnswerDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AssessmentAnswer.reconstruct(
                dataObject.getId(),
                dataObject.getUserId(),
                dataObject.getQuestionId(),
                dataObject.getContent(),
                dataObject.getLanguage(),
                dataObject.getFileId(),
                dataObject.getSubmitTime(),
                dataObject.getTeamId());
    }

    public List<AssessmentAnswerDO> toDataObjectList(List<AssessmentAnswer> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toDataObject).toList();
    }

    public List<AssessmentAnswer> toEntityList(List<AssessmentAnswerDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream().map(this::toEntity).toList();
    }
}
