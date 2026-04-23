package com.bluenet.web.application.command.college;

/**
 * 学院聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class CollegeCommands {

    /** 禁止实例化。 */
    private CollegeCommands() {
    }

    /**
     * 创建学院命令。
     * <p>
     * 用于创建新的学院。
     * </p>
     */
    public record CreateCollegeCommand(
            /** 名称 */
            String name) {
        public CreateCollegeCommand {
            if (name != null) {
                name = name.trim();
            }
        }
    }

    /**
     * 更新学院命令。
     * <p>
     * 用于更新已有的学院信息。
     * </p>
     */
    public record UpdateCollegeCommand(
            /** ID */
            Long id,
            /** 名称 */
            String name) {
        public UpdateCollegeCommand {
            if (name != null) {
                name = name.trim();
            }
        }
    }
}
