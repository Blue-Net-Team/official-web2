package com.bluenet.web.application.command.softwareresource;

import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;

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
            SoftwareResourceDirection direction,
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
            SoftwareResourceDirection direction,
            String category,
            String description,
            String externalUrl,
            Integer sortOrder,
            SoftwareResourceStatus status) {
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
