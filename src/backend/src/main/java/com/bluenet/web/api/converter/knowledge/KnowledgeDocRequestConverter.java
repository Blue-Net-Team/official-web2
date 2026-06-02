package com.bluenet.web.api.converter.knowledge;

import com.bluenet.web.api.dto.knowledge.UploadKnowledgeDocRequestDTO;
import com.bluenet.web.application.command.knowledge.KnowledgeCommands;
import org.springframework.stereotype.Component;

/**
 * 知识库文档请求转换器。
 */
@Component
public class KnowledgeDocRequestConverter {

    /**
     * 上传请求 → 上传命令
     */
    public KnowledgeCommands.UploadDocumentCommand toUploadCommand(UploadKnowledgeDocRequestDTO dto) {
        return new KnowledgeCommands.UploadDocumentCommand(dto.file(), dto.title());
    }
}
