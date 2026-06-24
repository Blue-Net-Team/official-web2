package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.infrastructure.repository.converter.CollegeRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.CollegeDO;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学院仓库实现类
 * <p>
 * 实现学院数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class CollegeRepositoryImpl implements CollegeRepository {
    private final CollegeMapper collegeMapper;
    private final UserMapper userMapper;
    private final EnrollMapper enrollMapper;
    private final CollegeRepositoryConverter converter;

    @Override
    public List<College> findAll() {
        List<CollegeDO> dataObjects = collegeMapper.selectList(null);
        return converter.toEntityList(dataObjects);
    }

    @Override
    public Optional<College> findById(Long id) {
        CollegeDO dataObject = collegeMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public Optional<College> findByName(String name) {
        CollegeDO collegeDO = collegeMapper.selectByName(name);
        return Optional.ofNullable(converter.toEntity(collegeDO));
    }

    @Override
    public void save(College college) {
        CollegeDO dataObject = converter.toDataObject(college);
        collegeMapper.insert(dataObject);
        college.setId(dataObject.getId());
    }

    @Override
    public void update(College college) {
        CollegeDO dataObject = converter.toDataObject(college);
        collegeMapper.updateById(dataObject);
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
        return collegeMapper.countByName(name) > 0;
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        return collegeMapper.countByNameAndIdNot(name, excludeId) > 0;
    }

    @Override
    public boolean hasAssociatedUsers(Long id) {
        return userMapper.countByCollegeId(id) > 0;
    }

    @Override
    public boolean hasAssociatedEnrolls(Long id) {
        return enrollMapper.countByCollegeId(id) > 0;
    }
}
