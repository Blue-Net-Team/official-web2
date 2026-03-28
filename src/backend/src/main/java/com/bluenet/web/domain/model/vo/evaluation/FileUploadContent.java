package com.bluenet.web.domain.model.vo.evaluation;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileUploadContent extends QuestionContent {
    // 文件上传题只有题干，无其他字段
}
