package com.bluenet.web.application.command.knowledge;

import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库聚合的命令对象集合。
 */
public class KnowledgeCommands {

    private KnowledgeCommands() {
    }

    /**
     * 上传知识库文档命令。
     */
    public record UploadDocumentCommand(
            MultipartFile file,
            String title) {
    }

    /**
     * 重新解析文档命令。
     */
    public record ReparseDocumentCommand(
            Long docId) {
    }

    /**
     * 取消解析命令。
     */
    public record CancelParseCommand(
            Long docId) {
    }

    /**
     * 删除文档命令。
     */
    public record DeleteDocumentCommand(
            Long docId) {
    }

    /**
     * 更新标签描述命令。
     */
    public record UpdateTagDescriptionCommand(
            Long tagId,
            String description) {
    }

    /**
     * 编辑分片命令。
     */
    public record UpdateChunkCommand(
            Long chunkId,
            String content,
            java.util.List<Long> tagIds) {
    }

    /**
     * 重新上传文档附件命令。
     */
    public record ReplaceDocFileCommand(
            Long docId,
            MultipartFile file) {
    }

    /**
     * 新建标签命令。
     */
    public record CreateTagCommand(
            String tagName,
            String description) {
    }

    /**
     * 更新标签命令（重命名/描述，字段为 null 表示不修改）。
     */
    public record UpdateTagCommand(
            Long tagId,
            String tagName,
            String description) {
    }

    /**
     * 删除标签命令。
     */
    public record DeleteTagCommand(
            Long tagId) {
    }
}
