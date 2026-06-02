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
}
