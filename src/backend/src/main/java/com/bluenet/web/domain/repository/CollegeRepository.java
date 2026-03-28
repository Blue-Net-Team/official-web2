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
     * 查询所有学院
     *
     * @return 学院列表
     */
    List<CollegeVO> findAll();

    /**
     * 根据ID查询学院
     *
     * @param id
     *            学院ID
     * @return 学院信息，如果不存在则返回Optional.empty()
     */
    Optional<CollegeVO> findById(Long id);

    /**
     * 保存学院
     *
     * @param name
     *            学院名称
     * @return 保存后的学院ID
     */
    Long save(String name);

    /**
     * 更新学院
     *
     * @param id
     *            学院ID
     * @param name
     *            学院名称
     */
    void update(Long id, String name);

    /**
     * 根据ID删除学院
     *
     * @param id
     *            学院ID
     */
    void deleteById(Long id);

    /**
     * 检查学院是否存在
     *
     * @param id
     *            学院ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);

    /**
     * 检查学院名称是否已存在
     *
     * @param name
     *            学院名称
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByName(String name);

    /**
     * 检查学院名称是否已存在（排除指定ID）
     *
     * @param name
     *            学院名称
     * @param excludeId
     *            排除的学院ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByNameAndIdNot(String name, Long excludeId);

    /**
     * 检查学院是否有关联的用户
     *
     * @param id
     *            学院ID
     * @return 如果有关联用户返回true，否则返回false
     */
    boolean hasAssociatedUsers(Long id);

    /**
     * 检查学院是否有关联的报名记录
     *
     * @param id
     *            学院ID
     * @return 如果有关联报名记录返回true，否则返回false
     */
    boolean hasAssociatedEnrolls(Long id);
}
