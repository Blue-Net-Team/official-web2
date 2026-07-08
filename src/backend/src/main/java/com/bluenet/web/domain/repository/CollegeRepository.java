package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.College;

import java.util.List;
import java.util.Optional;

/**
 * 学院仓库接口
 * <p>
 * 负责学院数据的持久化操作，只操作 Entity，不暴露 VO 或 DTO
 * </p>
 */
public interface CollegeRepository {
    /**
     * 查询全部学院记录。
     *
     * @return 学院实体集合。
     */
    List<College> findAll();

    /**
     * 按主键查询学院记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的学院实体；不存在时为 Optional.empty()。
     */
    Optional<College> findById(Long id);

    /**
     * 按名称查询学院记录。
     *
     * @param name
     *            学院名。
     * @return 查询到的学院实体；不存在时为 Optional.empty()。
     */
    Optional<College> findByName(String name);

    /**
     * 保存学院记录。
     *
     * @param college
     *            学院实体
     */
    void save(College college);

    /**
     * 删除指定学院记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的学院记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);

    /**
     * 判断是否存在满足条件的学院记录。
     *
     * @param name
     *            业务对象名称。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByName(String name);

    /**
     * 判断除当前记录外是否存在相同业务唯一键的学院记录。
     *
     * @param name
     *            业务对象名称。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByNameAndIdNot(String name, Long excludeId);

    /**
     * 判断学院下是否仍有关联用户。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean hasAssociatedUsers(Long id);

    /**
     * 判断学院下是否仍有关联报名申请。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean hasAssociatedEnrolls(Long id);
}
