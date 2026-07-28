package com.bluenet.web.application.command.userexperience;

import java.util.List;

/**
 * 用户经历聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class UserExperienceCommands {

    /** 禁止实例化。 */
    private UserExperienceCommands() {
    }

    /**
     * 创建经历命令。
     * <p>
     * 用于创建新的用户经历。
     * </p>
     */
    public record CreateExperienceCommand(
            /** 类型 */
            String type,
            /** 名称 */
            String name,
            /** 角色 */
            String role,
            /** 开始日期 */
            String startDate,
            /** 结束日期 */
            String endDate,
            /** 描述 */
            String description,
            /** 技术栈 */
            List<String> techStack,
            /** 演示URL */
            String demoUrl,
            /** 公司 */
            String company,
            /** 职位 */
            String position,
            /** 状态 */
            String status,
            /** 成就列表 */
            List<String> achievements) {
    }

    /**
     * 更新经历命令。
     * <p>
     * 用于更新已有的用户经历。
     * </p>
     */
    public record UpdateExperienceCommand(
            /** ID */
            Long id,
            /** 名称 */
            String name,
            /** 角色 */
            String role,
            /** 开始日期 */
            String startDate,
            /** 结束日期 */
            String endDate,
            /** 描述 */
            String description,
            /** 技术栈 */
            List<String> techStack,
            /** 演示URL */
            String demoUrl,
            /** 职位 */
            String position,
            /** 状态 */
            String status,
            /** 成就列表 */
            List<String> achievements) {
    }
}
