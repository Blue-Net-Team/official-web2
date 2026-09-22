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

    /**
     * 编辑分片内容与标签，并触发重新向量化。
     *
     * @param command
     *            编辑分片命令
     */
    void updateChunk(KnowledgeCommands.UpdateChunkCommand command);

    /**
     * 重新上传文档附件，触发完整重新解析。
     *
     * @param command
     *            换附件命令
     */
    void replaceDocFile(KnowledgeCommands.ReplaceDocFileCommand command);

    /**
     * 新建标签。
     *
     * @param command
     *            新建标签命令
     * @return 新建标签ID
     */
    Long createTag(KnowledgeCommands.CreateTagCommand command);

    /**
     * 更新标签（重命名/描述）。
     *
     * @param command
     *            更新标签命令
     */
    void updateTag(KnowledgeCommands.UpdateTagCommand command);

    /**
     * 删除标签，自动解除与全部分片的关联。
     *
     * @param command
     *            删除标签命令
     * @return 被解除关联的分片数量
     */
    int deleteTag(KnowledgeCommands.DeleteTagCommand command);
}
