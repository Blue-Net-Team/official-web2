package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.infrastructure.repository.converter.UserExperienceRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.UserExperienceDO;
import com.bluenet.web.infrastructure.repository.mapper.UserExperienceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户经历仓库实现类
 * <p>
 * 实现用户经历数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserExperienceRepositoryImpl implements UserExperienceRepository {
    private final UserExperienceMapper userExperienceMapper;
    private final UserExperienceRepositoryConverter converter;

    @Override
    public Optional<UserExperience> findById(Long id) {
        UserExperienceDO dataObject = userExperienceMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public List<UserExperience> findByUserId(Long userId) {
        List<UserExperienceDO> dataObjects = userExperienceMapper.selectByUserId(userId);
        return converter.toEntityList(dataObjects);
    }

    @Override
    public List<UserExperience> findByUserIdAndType(Long userId, ExperienceType type) {
        List<UserExperienceDO> dataObjects = userExperienceMapper.selectByUserIdAndType(userId, type);
        return converter.toEntityList(dataObjects);
    }

    @Override
    public void save(UserExperience entity) {
        UserExperienceDO dataObject = converter.toDataObject(entity);
        userExperienceMapper.insert(dataObject);
        entity.setId(dataObject.getId());
        log.info("Created experience: id={}, userId={}, type={}", entity.getId(), entity.getUserId(), entity.getType());
    }

    @Override
    public void update(UserExperience entity) {
        UserExperienceDO dataObject = converter.toDataObject(entity);
        userExperienceMapper.updateById(dataObject);
        log.info("Updated experience: id={}", entity.getId());
    }

    @Override
    public void deleteById(Long id) {
        userExperienceMapper.deleteById(id);
        log.info("Deleted experience: id={}", id);
    }

    @Override
    public int countByUserIdAndType(Long userId, ExperienceType type) {
        return Math.toIntExact(userExperienceMapper.countByUserIdAndType(userId, type));
    }

    @Override
    public boolean checkOwner(Long experienceId, Long userId) {
        UserExperienceDO dataObject = userExperienceMapper.selectById(experienceId);
        return dataObject != null && dataObject.getUserId().equals(userId);
    }
}
