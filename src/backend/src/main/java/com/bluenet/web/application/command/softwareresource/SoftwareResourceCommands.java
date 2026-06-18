package com.bluenet.web.application.command.softwareresource;

/**
 * 软件资源聚合的命令对象集合。
 */
public class SoftwareResourceCommands {

    private SoftwareResourceCommands() {
    }

    /**
     * 创建软件资源命令。
     */
    public record CreateSoftwareResourceCommand(
            String name,
            com.bluenet.web.domain.model.enumerate.Direction direction,
            String category,
            String description,
            String externalUrl,
            Integer sortOrder) {
        public CreateSoftwareResourceCommand {
            if (name != null) {
                name = name.trim();
            }
            if (externalUrl != null) {
                externalUrl = externalUrl.trim();
            }
        }
    }

    /**
     * 更新软件资源命令。
     */
    public record UpdateSoftwareResourceCommand(
            Long id,
            String name,
            com.bluenet.web.domain.model.enumerate.Direction direction,
            String category,
            String description,
            String externalUrl,
            Integer sortOrder,
            com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus status) {
        public UpdateSoftwareResourceCommand {
            if (name != null) {
                name = name.trim();
            }
            if (externalUrl != null) {
                externalUrl = externalUrl.trim();
            }
        }
    }
}
