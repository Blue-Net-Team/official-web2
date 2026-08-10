package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;

import java.util.List;
import java.util.Optional;

/**
 * 用户经历仓储接口
 * <p>
 * 负责用户经历数据的持久化操作，只操作 Entity，不暴露 VO 或 DTO
 * </p>
 */
public interface UserExperienceRepository {
    /**
     * 按主键查询用户经历记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的用户经历实体；不存在时为空。
     */
    Optional<UserExperience> findById(Long id);

    /**
     * 查询指定用户关联的用户经历记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 满足条件的用户经历实体集合。
     */
    List<UserExperience> findByUserId(Long userId);

    /**
     * 查询用户指定类型的经历列表。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的用户经历实体集合。
     */
    List<UserExperience> findByUserIdAndType(Long userId, ExperienceType type);

    /**
     * 保存新的用户经历记录。
     *
     * @param entity
     *            用户经历实体
     */
    void save(UserExperience entity);

    /**
     * 删除指定用户经历记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 统计用户指定类型的经历数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的记录数量。
     */
    int countByUserIdAndType(Long userId, ExperienceType type);

    /**
     * 校验用户是否拥有指定经历记录。
     *
     * @param experienceId
     *            用户经历记录主键。
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean checkOwner(Long experienceId, Long userId);
}
