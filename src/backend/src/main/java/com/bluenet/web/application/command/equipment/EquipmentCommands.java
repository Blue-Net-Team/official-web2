package com.bluenet.web.application.command.equipment;

/**
 * 设备聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class EquipmentCommands {

    /** 禁止实例化。 */
    private EquipmentCommands() {
    }

    /**
     * 创建设备命令。
     * <p>
     * 用于创建新的设备信息。
     * </p>
     */
    public record CreateEquipmentCommand(
            /** 名称 */
            String name,
            /** 品牌 */
            String brand,
            /** 描述 */
            String description,
            /** 图片文件ID */
            Long imageFileId,
            /** 排序顺序 */
            Integer sortOrder) {
        public CreateEquipmentCommand {
            if (name != null) {
                name = name.trim();
            }
        }
    }

    /**
     * 更新设备命令。
     * <p>
     * 用于更新已有的设备信息。
     * </p>
     */
    public record UpdateEquipmentCommand(
            /** ID */
            Long id,
            /** 名称 */
            String name,
            /** 品牌 */
            String brand,
            /** 描述 */
            String description,
            /** 图片文件ID */
            Long imageFileId,
            /** 排序顺序 */
            Integer sortOrder) {
        public UpdateEquipmentCommand {
            if (name != null) {
                name = name.trim();
            }
        }
    }
}
