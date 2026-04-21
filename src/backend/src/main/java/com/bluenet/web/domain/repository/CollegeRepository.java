package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.CollegeVO;

import java.util.List;
import java.util.Optional;

/**
 * 学院仓库接口
 * <p>
 * 负责学院数据的持久化操作，包括增删改查等基本操作
 * </p>
 */
public interface CollegeRepository {
    /**
     * 查询全部学院 记录。
     *
     * @return 满足条件的学院 结果集合。
     */
    List<CollegeVO> findAll();

    /**
     * 处理学院 仓储职责中的业务数据访问逻辑。
     *
     * @param id
     *            业务记录主键。
     * @return 查询或处理得到的学院 结果。
     */
    Optional<CollegeVO> findById(Long id);

    /**
     * 保存新的学院 记录。
     *
     * @param name
     *            业务对象名称。
     * @return 新记录的主键。
     */
    Long save(String name);

    /**
     * 更新已有学院 记录。
     *
     * @param id
     *            业务记录主键。
     * @param name
     *            业务对象名称。
     */
    void update(Long id, String name);

    /**
     * 删除指定学院 记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的学院 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);

    /**
     * 判断是否存在满足条件的学院 记录。
     *
     * @param name
     *            业务对象名称。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByName(String name);

    /**
     * 判断除当前记录外是否存在相同业务唯一键的学院 记录。
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
