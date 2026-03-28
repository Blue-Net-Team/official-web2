package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.vo.EnrollBriefVO;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.EnrollVO;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class EnrollRepositoryImpl implements EnrollRepository {
    private final EnrollMapper enrollMapper;
    private final CollegeMapper collegeMapper;
    private final FileMapper fileMapper;
    private final UserMapper userMapper;

    @Override
    public Optional<EnrollVO> findById(Long id) {
        Enroll enroll = enrollMapper.selectById(id);
        if (enroll == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(enroll));
    }

    @Override
    public Optional<EnrollVO> findByStudentId(String studentId) {
        LambdaQueryWrapper<Enroll> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enroll::getStudentId, studentId);
        Enroll enroll = enrollMapper.selectOne(wrapper);
        if (enroll == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(enroll));
    }

    @Override
    public boolean existsByStudentId(String studentId) {
        LambdaQueryWrapper<Enroll> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enroll::getStudentId, studentId);
        return enrollMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Long save(EnrollVO enrollVO) {
        Enroll enroll = convertToEntity(enrollVO);
        enrollMapper.insert(enroll);
        return enroll.getId();
    }

    @Override
    public void update(EnrollVO enrollVO) {
        Enroll enroll = convertToEntity(enrollVO);
        enrollMapper.updateById(enroll);
    }

    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> findAll(
            org.springframework.data.domain.Pageable pageable) {
        Page<Enroll> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Enroll> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Enroll::getId);
        IPage<Enroll> result = enrollMapper.selectPage(page, wrapper);
        return convertToBriefPage(result, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> findByStatus(EnrollStatus status,
            org.springframework.data.domain.Pageable pageable) {
        Page<Enroll> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Enroll> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enroll::getStatus, status);
        wrapper.orderByDesc(Enroll::getId);
        IPage<Enroll> result = enrollMapper.selectPage(page, wrapper);
        return convertToBriefPage(result, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> findByDirection(Direction direction,
            org.springframework.data.domain.Pageable pageable) {
        Page<Enroll> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Enroll> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enroll::getDirection, direction);
        wrapper.orderByDesc(Enroll::getId);
        IPage<Enroll> result = enrollMapper.selectPage(page, wrapper);
        return convertToBriefPage(result, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> findByStatusAndDirection(EnrollStatus status,
            Direction direction, org.springframework.data.domain.Pageable pageable) {
        Page<Enroll> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Enroll> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enroll::getStatus, status);
        wrapper.eq(Enroll::getDirection, direction);
        wrapper.orderByDesc(Enroll::getId);
        IPage<Enroll> result = enrollMapper.selectPage(page, wrapper);
        return convertToBriefPage(result, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> search(String keyword, EnrollStatus status,
            Direction direction, org.springframework.data.domain.Pageable pageable) {
        Page<Enroll> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Enroll> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Enroll::getUsername, keyword).or().like(Enroll::getStudentId, keyword));
        }
        if (status != null) {
            wrapper.eq(Enroll::getStatus, status);
        }
        if (direction != null) {
            wrapper.eq(Enroll::getDirection, direction);
        }
        wrapper.orderByDesc(Enroll::getId);

        IPage<Enroll> result = enrollMapper.selectPage(page, wrapper);
        return convertToBriefPage(result, pageable);
    }

    @Override
    public EnrollStatisticsVO getStatistics() {
        LambdaQueryWrapper<Enroll> wrapper = new LambdaQueryWrapper<>();
        long total = enrollMapper.selectCount(wrapper);

        Map<String, Long> byStatus = new HashMap<>();
        for (EnrollStatus status : EnrollStatus.values()) {
            LambdaQueryWrapper<Enroll> statusWrapper = new LambdaQueryWrapper<>();
            statusWrapper.eq(Enroll::getStatus, status);
            byStatus.put(status.getValue(), enrollMapper.selectCount(statusWrapper));
        }

        Map<Direction, Long> byDirection = new HashMap<>();
        for (Direction direction : Direction.values()) {
            LambdaQueryWrapper<Enroll> dirWrapper = new LambdaQueryWrapper<>();
            dirWrapper.eq(Enroll::getDirection, direction);
            byDirection.put(direction, enrollMapper.selectCount(dirWrapper));
        }

        return EnrollStatisticsVO.builder()
                .total(total)
                .byStatus(byStatus)
                .byDirection(byDirection)
                .build();
    }

    private org.springframework.data.domain.Page<EnrollBriefVO> convertToBriefPage(IPage<Enroll> page,
            org.springframework.data.domain.Pageable pageable) {
        List<EnrollBriefVO> content = page.getRecords()
                .stream()
                .map(this::convertToBriefVO)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotal());
    }

    private EnrollBriefVO convertToBriefVO(Enroll enroll) {
        String collegeName = getCollegeName(enroll.getCollegeId());
        return EnrollBriefVO.builder()
                .id(enroll.getId())
                .username(enroll.getUsername())
                .studentId(enroll.getStudentId())
                .email(enroll.getEmail())
                .collegeName(collegeName)
                .major(enroll.getMajor())
                .grade(enroll.getGrade())
                .direction(enroll.getDirection())
                .status(enroll.getStatus())
                .avatarFileId(enroll.getAvatarId())
                .build();
    }

    private EnrollVO convertToVO(Enroll enroll) {
        String collegeName = getCollegeName(enroll.getCollegeId());
        String referralUserName = getReferralUserName(enroll.getInternalReferralCode());
        Long referralUserId = getReferralUserId(enroll.getInternalReferralCode());

        return EnrollVO.builder()
                .id(enroll.getId())
                .username(enroll.getUsername())
                .studentId(enroll.getStudentId())
                .email(enroll.getEmail())
                .collegeId(enroll.getCollegeId())
                .collegeName(collegeName)
                .major(enroll.getMajor())
                .grade(enroll.getGrade())
                .direction(enroll.getDirection())
                .avatarFileId(enroll.getAvatarId())
                .status(enroll.getStatus())
                .introduction(enroll.getIntroduction())
                .internalReferralCode(enroll.getInternalReferralCode())
                .referralUserId(referralUserId)
                .referralUserName(referralUserName)
                .build();
    }

    private Enroll convertToEntity(EnrollVO vo) {
        return Enroll.builder()
                .id(vo.getId())
                .username(vo.getUsername())
                .studentId(vo.getStudentId())
                .password(vo.getPassword())
                .email(vo.getEmail())
                .collegeId(vo.getCollegeId())
                .major(vo.getMajor())
                .grade(vo.getGrade())
                .direction(vo.getDirection())
                .avatarId(vo.getAvatarFileId())
                .status(vo.getStatus())
                .introduction(vo.getIntroduction())
                .internalReferralCode(vo.getInternalReferralCode())
                .build();
    }

    private String getCollegeName(Long collegeId) {
        if (collegeId == null) {
            return null;
        }
        College college = collegeMapper.selectById(collegeId);
        return college != null ? college.getName() : null;
    }

    private String getReferralUserName(String internalReferralCode) {
        if (internalReferralCode == null || internalReferralCode.trim().isEmpty()) {
            return null;
        }
        User user = userMapper.selectByInternalReferralCode(internalReferralCode);
        return user != null ? user.getUsername() : null;
    }

    private Long getReferralUserId(String internalReferralCode) {
        if (internalReferralCode == null || internalReferralCode.trim().isEmpty()) {
            return null;
        }
        User user = userMapper.selectByInternalReferralCode(internalReferralCode);
        return user != null ? user.getId() : null;
    }
}
