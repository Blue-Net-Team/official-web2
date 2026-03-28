package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.CollegeVO;

import java.util.List;
import java.util.Optional;

/**
 * 学院领域服务接口
 * <p>
 * 提供学院相关的业务逻辑操作，包括学院的查询、创建、更新、删除等功能
 * </p>
 */
public interface CollegeDomainService {
    /**
     * 获取所有学院
     *
     * @return 学院列表
     */
    List<CollegeVO> getAllColleges();

    /**
     * 根据ID获取学院
     *
     * @param id
     *            学院ID
     * @return 学院信息，如果不存在则返回Optional.empty()
     */
    Optional<CollegeVO> getCollegeById(Long id);

    /**
     * 创建学院
     *
     * @param name
     *            学院名称
     * @return 创建后的学院ID
     * @throws IllegalArgumentException
     *             如果学院名称已存在
     */
    Long createCollege(String name);

    /**
     * 更新学院
     *
     * @param id
     *            学院ID
     * @param name
     *            学院名称
     * @throws IllegalArgumentException
     *             如果学院不存在或名称已被其他学院使用
     */
    void updateCollege(Long id, String name);

    /**
     * 删除学院
     *
     * @param id
     *            学院ID
     * @throws IllegalArgumentException
     *             如果学院不存在或有关联数据
     */
    void deleteCollege(Long id);

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
     * 检查学院是否可以删除
     *
     * @param id
     *            学院ID
     * @return 如果可以删除返回true，否则返回false
     */
    boolean canDelete(Long id);
}
