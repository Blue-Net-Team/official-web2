package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.vo.CollegeVO;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 学院仓库实现类
 * <p>
 * 实现学院数据的持久化操作
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class CollegeRepositoryImpl implements CollegeRepository {
    private final CollegeMapper collegeMapper;
    private final UserMapper userMapper;
    private final EnrollMapper enrollMapper;

    /**
     * 查询全部学院 记录。
     *
     * @return 满足条件的学院 结果集合。
     */
    @Override
    public List<CollegeVO> findAll() {
        List<College> colleges = RepositoryObjectConverter.toDomainList(collegeMapper.selectList(null), College.class);
        return colleges.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 按主键查询学院 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的学院 结果；不存在时为空。
     */
    @Override
    public Optional<CollegeVO> findById(Long id) {
        College college = RepositoryObjectConverter.toDomain(collegeMapper.selectById(id), College.class);
        if (college == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(college));
    }

    /**
     * 保存新的学院 记录。
     *
     * @param name
     *            业务对象名称。
     * @return 新记录的主键。
     */
    @Override
    public Long save(String name) {
        College college = new College();
        college.setName(name);
        RepositoryObjectConverter.insert(collegeMapper, college, CollegeDO.class);
        return college.getId();
    }

    /**
     * 更新已有学院 记录。
     *
     * @param id
     *            业务记录主键。
     * @param name
     *            业务对象名称。
     */
    @Override
    public void update(Long id, String name) {
        College college = new College();
        college.setId(id);
        college.setName(name);
        RepositoryObjectConverter.updateById(collegeMapper, college, CollegeDO.class);
    }

    /**
     * 删除指定学院 记录。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    public void deleteById(Long id) {
        collegeMapper.deleteById(id);
    }

    /**
     * 判断是否存在满足条件的学院 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsById(Long id) {
        return collegeMapper.selectById(id) != null;
    }

    /**
     * 判断是否存在满足条件的学院 记录。
     *
     * @param name
     *            业务对象名称。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsByName(String name) {
        return collegeMapper.countByName(name) > 0;
    }

    /**
     * 判断除当前记录外是否存在相同业务唯一键的学院 记录。
     *
     * @param name
     *            业务对象名称。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        return collegeMapper.countByNameAndIdNot(name, excludeId) > 0;
    }

    /**
     * 判断学院下是否仍有关联用户。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean hasAssociatedUsers(Long id) {
        return userMapper.countByCollegeId(id) > 0;
    }

    /**
     * 判断学院下是否仍有关联报名申请。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean hasAssociatedEnrolls(Long id) {
        return enrollMapper.countByCollegeId(id) > 0;
    }

    /**
     * 在学院 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param college
     *            学院名称。
     * @return 转换后的目标模型对象。
     */
    private CollegeVO convertToVO(College college) {
        return CollegeVO.builder()
                .id(college.getId())
                .name(college.getName())
                .build();
    }
}
