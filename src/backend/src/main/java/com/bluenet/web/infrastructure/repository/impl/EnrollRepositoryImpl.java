package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.infrastructure.repository.converter.CollegeRepositoryConverter;
import com.bluenet.web.infrastructure.repository.converter.EnrollRepositoryConverter;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.infrastructure.repository.dataobject.CollegeDO;
import com.bluenet.web.infrastructure.repository.dataobject.EnrollDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class EnrollRepositoryImpl implements EnrollRepository {
    private final EnrollMapper enrollMapper;
    private final CollegeMapper collegeMapper;
    private final UserMapper userMapper;
    private final EnrollRepositoryConverter converter;
    private final CollegeRepositoryConverter collegeConverter;

    @Override
    public Optional<Enroll> findById(Long id) {
        EnrollDO dataObject = enrollMapper.selectById(id);
        return Optional.ofNullable(toEntity(dataObject, true));
    }

    @Override
    public Optional<Enroll> findByStudentId(String studentId) {
        EnrollDO dataObject = enrollMapper.selectByStudentId(studentId);
        return Optional.ofNullable(toEntity(dataObject, true));
    }

    @Override
    public boolean existsByStudentId(String studentId) {
        return enrollMapper.countByStudentId(studentId) > 0;
    }

    @Override
    public void save(Enroll enroll) {
        EnrollDO dataObject = converter.toDataObject(enroll);
        enrollMapper.insert(dataObject);
        enroll.setId(dataObject.getId());
    }

    @Override
    public void update(Enroll enroll) {
        EnrollDO dataObject = converter.toDataObject(enroll);
        enrollMapper.updateById(dataObject);
    }

    @Override
    public org.springframework.data.domain.Page<Enroll> findAll(Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, null, null, null);
        return convertToEntityPage(result, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Enroll> findByStatus(EnrollStatus status, Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, null, status, null);
        return convertToEntityPage(result, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Enroll> findByDirection(Direction direction, Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, null, null, direction);
        return convertToEntityPage(result, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Enroll> findByStatusAndDirection(EnrollStatus status,
            Direction direction, Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, null, status, direction);
        return convertToEntityPage(result, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Enroll> search(String keyword, EnrollStatus status, Direction direction,
            Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, normalizedKeyword, status, direction);
        return convertToEntityPage(result, pageable);
    }

    @Override
    public EnrollStatisticsVO getStatistics() {
        long total = enrollMapper.countAll();

        Map<String, Long> byStatus = new HashMap<>();
        for (EnrollStatus status : EnrollStatus.values()) {
            byStatus.put(status.getValue(), enrollMapper.countByStatus(status));
        }

        Map<Direction, Long> byDirection = new HashMap<>();
        for (Direction direction : Direction.values()) {
            byDirection.put(direction, enrollMapper.countByDirection(direction));
        }

        return EnrollStatisticsVO.builder()
                .total(total)
                .byStatus(byStatus)
                .byDirection(byDirection)
                .build();
    }

    private org.springframework.data.domain.Page<Enroll> convertToEntityPage(IPage<EnrollDO> page, Pageable pageable) {
        List<Enroll> content = page.getRecords()
                .stream()
                .map(doObj -> toEntity(doObj, false))
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotal());
    }

    private Enroll toEntity(EnrollDO dataObject, boolean includeReferral) {
        if (dataObject == null) {
            return null;
        }
        String collegeName = getCollegeName(dataObject.getCollegeId());
        String referralUserName = null;
        Long referralUserId = null;
        if (includeReferral && dataObject.getInternalReferralCode() != null) {
            referralUserName = getReferralUserName(dataObject.getInternalReferralCode());
            referralUserId = getReferralUserId(dataObject.getInternalReferralCode());
        }
        return converter.toEntity(dataObject, collegeName, referralUserId, referralUserName);
    }

    private String getCollegeName(Long collegeId) {
        if (collegeId == null) {
            return null;
        }
        CollegeDO collegeDO = collegeMapper.selectById(collegeId);
        College college = collegeConverter.toEntity(collegeDO);
        return college != null ? college.getName() : null;
    }

    private String getReferralUserName(String internalReferralCode) {
        if (internalReferralCode == null || internalReferralCode.trim().isEmpty()) {
            return null;
        }
        User user = toUser(userMapper.selectByInternalReferralCode(internalReferralCode));
        return user != null ? user.getUsername() : null;
    }

    private Long getReferralUserId(String internalReferralCode) {
        if (internalReferralCode == null || internalReferralCode.trim().isEmpty()) {
            return null;
        }
        User user = toUser(userMapper.selectByInternalReferralCode(internalReferralCode));
        return user != null ? user.getId() : null;
    }

    private User toUser(UserDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return User.reconstruct(
                dataObject.getId(),
                dataObject.getStudentId(),
                dataObject.getEmail(),
                dataObject.getRoleId(),
                dataObject.getPassword(),
                dataObject.getUsername(),
                dataObject.getNickname(),
                dataObject.getCollegeId(),
                dataObject.getMajor(),
                dataObject.getAssessmentGradeYear(),
                dataObject.getDirection(),
                dataObject.getGender(),
                dataObject.getJob(),
                dataObject.getAvatarId(),
                dataObject.getDisable(),
                dataObject.getQrcodeId(),
                dataObject.getGithubId(),
                dataObject.getGithubUsername(),
                dataObject.getInternalReferralCode(),
                dataObject.getBio());
    }
}
