package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.question_content.QuestionContent;
import com.bluenet.web.infrastructure.repository.handler.QuestionContentTypeHandler;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tb_assessment_question", autoResultMap = true)
public class AssessmentQuestionDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属考核场次或考核时间配置标识。
     */
    private Long assessmentTimeId;
    /**
     * 题目在考核场次中的序号。
     */
    private Integer questionNo;

    /**
     * 题目类型，例如算法题、单选题或多选题。
     */
    private QuestionType questionType;
    /**
     * 标题或名称，用于列表和详情展示。
     */
    private String title;

    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    @TableField(typeHandler = QuestionContentTypeHandler.class)
    private QuestionContent content;
    /**
     * 题目或记录绑定的附件文件标识。
     */
    private Long attachmentId;

    /**
     * 答案、题目或评审记录在考核中的得分。
     */
    private BigDecimal score;
}
