package com.bluenet.web.domain.model.enumerate;

/**
 * 角色类型枚举 定义系统中的所有角色。
 *
 * <p>
 * 角色层级关系（从高到低）：
 * <ul>
 * <li>SUPER_ADMIN (4) - 超级管理员，团队负责人或Web技术负责人</li>
 * <li>DIRECTION_ADMIN (3) - 方向管理员，各方向（视觉、结构、电控等）的管理员</li>
 * <li>MEMBER (2) - 团队成员，正式团队成员</li>
 * <li>CANDIDATE (1) - 考生，已发放账号、正在考核中的用户</li>
 * </ul>
 *
 * <p>
 * <b>使用规范：</b>
 * <ul>
 * <li>禁止在代码中硬编码角色字符串（如 "ADMIN"、"MEMBER"）</li>
 * <li>使用 {@link RoleType} 枚举常量进行角色比较</li>
 * <li>使用领域角色层级规则进行权限判断</li>
 * <li>数据库中的角色名称必须与 {@link #getName()} 返回值一致</li>
 * </ul>
 *
 * @see com.bluenet.web.domain.model.policy.RoleHierarchy
 */
public enum RoleType {
    /**
     * 超级管理员。
     * <p>
     * 团队负责人或Web技术负责人，拥有系统所有权限。
     * </p>
     */
    SUPER_ADMIN("SUPER_ADMIN", 4),

    /**
     * 方向管理员。
     * <p>
     * 各方向（视觉、结构、电控等）的管理员，可管理对应方向的考核和报名。
     * </p>
     */
    DIRECTION_ADMIN("DIRECTION_ADMIN", 3),

    /**
     * 团队成员。
     * <p>
     * 正式团队成员，可查看考核作品、参与评分等。
     * </p>
     */
    MEMBER("MEMBER", 2),

    /**
     * 考生。
     * <p>
     * 已发放账号、正在考核中的用户，权限最低。
     * </p>
     */
    CANDIDATE("CANDIDATE", 1);

    private final String name;
    private final int level;

    RoleType(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    /**
     * 检查当前角色是否大于等于目标角色
     *
     * @param target
     *            目标角色
     * @return true 如果当前角色级别 >= 目标角色级别
     */
    public boolean isAtLeast(RoleType target) {
        return this.level >= target.level;
    }

    /**
     * 根据名称获取角色类型
     *
     * @param name
     *            角色名称
     * @return 角色类型，如果找不到返回null
     */
    public static RoleType fromName(String name) {
        for (RoleType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
