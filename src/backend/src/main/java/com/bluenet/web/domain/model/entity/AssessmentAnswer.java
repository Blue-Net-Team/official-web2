package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_assessment_answer")
public class AssessmentAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    private String content;
    private ProgrammingLanguage language;
    private Long fileId;
    private LocalDateTime submitTime;
}
