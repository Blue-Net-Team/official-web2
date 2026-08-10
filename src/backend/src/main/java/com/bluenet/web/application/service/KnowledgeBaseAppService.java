package com.bluenet.web.application.service;

import com.bluenet.web.application.command.knowledge.KnowledgeCommands;
import com.bluenet.web.application.result.knowledge.KnowledgeDocResult;

/**
 * 知识库应用服务接口。
 */
public interface KnowledgeBaseAppService {

    /**
     * 上传知识库文档。
     *
     * @param command
     *            上传命令
     * @return 上传结果
     */
    KnowledgeDocResult.Uploaded uploadDocument(KnowledgeCommands.UploadDocumentCommand command);

    /**
     * 重新解析文档。
     *
     * @param command
     *            重新解析命令
     */
    void reparse(KnowledgeCommands.ReparseDocumentCommand command);

    /**
     * 取消解析。
     *
     * @param command
     *            取消命令
     */
    void cancelParse(KnowledgeCommands.CancelParseCommand command);

    /**
     * 删除文档。
     *
     * @param command
     *            删除命令
     */
    void deleteDocument(KnowledgeCommands.DeleteDocumentCommand command);

    /**
     * 更新标签描述。
     *
     * @param tagId
     *            标签ID
     * @param description
     *            新描述
     */
    void updateTagDescription(Long tagId, String description);
}
