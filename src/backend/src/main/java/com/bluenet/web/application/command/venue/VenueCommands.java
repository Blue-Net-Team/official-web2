package com.bluenet.web.application.command.venue;

/**
 * 场地聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class VenueCommands {

    /** 禁止实例化。 */
    private VenueCommands() {
    }

    /**
     * 创建场地命令。
     * <p>
     * 用于创建新的场地信息。
     * </p>
     */
    public record CreateVenueCommand(
            /** 名称 */
            String name,
            /** 副标题 */
            String subtitle,
            /** 描述 */
            String description,
            /** 图片文件ID */
            Long imageFileId,
            /** 排序顺序 */
            Integer sortOrder) {
        public CreateVenueCommand {
            if (name != null) {
                name = name.trim();
            }
        }
    }

    /**
     * 更新场地命令。
     * <p>
     * 用于更新已有的场地信息。
     * </p>
     */
    public record UpdateVenueCommand(
            /** ID */
            Long id,
            /** 名称 */
            String name,
            /** 副标题 */
            String subtitle,
            /** 描述 */
            String description,
            /** 图片文件ID */
            Long imageFileId,
            /** 排序顺序 */
            Integer sortOrder) {
        public UpdateVenueCommand {
            if (name != null) {
                name = name.trim();
            }
        }
    }
}
