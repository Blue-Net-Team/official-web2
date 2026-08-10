package com.bluenet.web.domain.model.vo.question_content;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileUploadContent extends QuestionContent {
    // 文件上传题只有题干，无其他字段

    @Override
    public void sanitizeForUser() {
        // 文件上传题无需擦除任何信息
    }
}
