package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.entity.User;
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

    @Override
    public List<CollegeVO> findAll() {
        List<College> colleges = collegeMapper.selectList(null);
        return colleges.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CollegeVO> findById(Long id) {
        College college = collegeMapper.selectById(id);
        if (college == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(college));
    }

    @Override
    public Long save(String name) {
        College college = new College();
        college.setName(name);
        collegeMapper.insert(college);
        return college.getId();
    }

    @Override
    public void update(Long id, String name) {
        College college = new College();
        college.setId(id);
        college.setName(name);
        collegeMapper.updateById(college);
    }

    @Override
    public void deleteById(Long id) {
        collegeMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return collegeMapper.selectById(id) != null;
    }

    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(College::getName, name);
        return collegeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(College::getName, name)
                .ne(College::getId, excludeId);
        return collegeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean hasAssociatedUsers(Long id) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getCollegeId, id);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean hasAssociatedEnrolls(Long id) {
        LambdaQueryWrapper<Enroll> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enroll::getCollegeId, id);
        return enrollMapper.selectCount(wrapper) > 0;
    }

    /**
     * 将实体转换为VO
     *
     * @param college
     *            学院实体
     * @return 学院VO
     */
    private CollegeVO convertToVO(College college) {
        return CollegeVO.builder()
                .id(college.getId())
                .name(college.getName())
                .build();
    }
}
