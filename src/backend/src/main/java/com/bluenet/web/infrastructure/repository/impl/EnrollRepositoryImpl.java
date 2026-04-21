package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

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

    /**
     * 按主键查询报名申请 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的报名申请 结果；不存在时为空。
     */
    @Override
    public Optional<EnrollVO> findById(Long id) {
        Enroll enroll = RepositoryObjectConverter.toDomain(enrollMapper.selectById(id), Enroll.class);
        if (enroll == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(enroll));
    }

    /**
     * 按学号查询用户或报名申请。
     *
     * @param studentId
     *            学生学号，用于定位用户或报名申请。
     * @return 查询到的报名申请 结果；不存在时为空。
     */
    @Override
    public Optional<EnrollVO> findByStudentId(String studentId) {
        Enroll enroll = RepositoryObjectConverter.toDomain(enrollMapper.selectByStudentId(studentId), Enroll.class);
        if (enroll == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(enroll));
    }

    /**
     * 判断是否存在满足条件的报名申请 记录。
     *
     * @param studentId
     *            学生学号，用于定位用户或报名申请。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsByStudentId(String studentId) {
        return enrollMapper.countByStudentId(studentId) > 0;
    }

    /**
     * 保存新的报名申请 记录。
     *
     * @param enrollVO
     *            报名申请视图对象。
     * @return 新记录的主键。
     */
    @Override
    public Long save(EnrollVO enrollVO) {
        Enroll enroll = convertToEntity(enrollVO);
        RepositoryObjectConverter.insert(enrollMapper, enroll, EnrollDO.class);
        return enroll.getId();
    }

    /**
     * 更新已有报名申请 记录。
     *
     * @param enrollVO
     *            报名申请视图对象。
     */
    @Override
    public void update(EnrollVO enrollVO) {
        Enroll enroll = convertToEntity(enrollVO);
        RepositoryObjectConverter.updateById(enrollMapper, enroll, EnrollDO.class);
    }

    /**
     * 查询全部报名申请 记录。
     *
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名申请 结果。
     */
    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> findAll(
            org.springframework.data.domain.Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, null, null, null);
        return convertToBriefPage(result, pageable);
    }

    /**
     * 按业务状态查询报名申请 记录。
     *
     * @param status
     *            业务状态过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名申请 结果。
     */
    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> findByStatus(EnrollStatus status,
            org.springframework.data.domain.Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, null, status, null);
        return convertToBriefPage(result, pageable);
    }

    /**
     * 按技术方向查询报名申请 记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名申请 结果。
     */
    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> findByDirection(Direction direction,
            org.springframework.data.domain.Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, null, null, direction);
        return convertToBriefPage(result, pageable);
    }

    /**
     * 按报名状态和技术方向查询报名申请。
     *
     * @param status
     *            业务状态过滤条件。
     * @param direction
     *            技术方向过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名申请 结果。
     */
    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> findByStatusAndDirection(EnrollStatus status,
            Direction direction, org.springframework.data.domain.Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, null, status, direction);
        return convertToBriefPage(result, pageable);
    }

    /**
     * 按关键字搜索报名申请 记录。
     *
     * @param keyword
     *            搜索关键字。
     * @param status
     *            业务状态过滤条件。
     * @param direction
     *            技术方向过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名申请 结果。
     */
    @Override
    public org.springframework.data.domain.Page<EnrollBriefVO> search(String keyword, EnrollStatus status,
            Direction direction, org.springframework.data.domain.Pageable pageable) {
        Page<EnrollDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        IPage<EnrollDO> result = enrollMapper.selectPageByConditions(page, normalizedKeyword, status, direction);
        return convertToBriefPage(result, pageable);
    }

    /**
     * 汇总报名申请 相关的状态和方向统计数据。
     *
     * @return 查询或处理得到的报名申请 结果。
     */
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

    /**
     * 将报名分页数据转换为报名摘要分页视图。
     *
     * @param page
     *            分页请求或 MyBatis-Plus 分页对象。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名申请 结果。
     */
    private org.springframework.data.domain.Page<EnrollBriefVO> convertToBriefPage(IPage<EnrollDO> page,
            org.springframework.data.domain.Pageable pageable) {
        // 分页记录先从 DO 转为领域对象，再复用原有展示 VO 组装逻辑。
        List<EnrollBriefVO> content = page.getRecords()
                .stream()
                .map(enroll -> RepositoryObjectConverter.toDomain(enroll, Enroll.class))
                .map(this::convertToBriefVO)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotal());
    }

    /**
     * 将报名领域对象转换为列表摘要视图。
     *
     * @param enroll
     *            报名申请领域对象。
     * @return 转换后的目标模型对象。
     */
    private EnrollBriefVO convertToBriefVO(Enroll enroll) {
        String collegeName = getCollegeName(enroll.getCollegeId());
        return EnrollBriefVO.builder()
                .id(enroll.getId())
                .username(enroll.getUsername())
                .studentId(enroll.getStudentId())
                .email(enroll.getEmail())
                .collegeName(collegeName)
                .major(enroll.getMajor())
                .gender(enroll.getGender())
                .direction(enroll.getDirection())
                .status(enroll.getStatus())
                .avatarFileId(enroll.getAvatarId())
                .build();
    }

    /**
     * 在报名申请 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param enroll
     *            报名申请领域对象。
     * @return 转换后的目标模型对象。
     */
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
                .gender(enroll.getGender())
                .direction(enroll.getDirection())
                .avatarFileId(enroll.getAvatarId())
                .status(enroll.getStatus())
                .introduction(enroll.getIntroduction())
                .internalReferralCode(enroll.getInternalReferralCode())
                .referralUserId(referralUserId)
                .referralUserName(referralUserName)
                .build();
    }

    /**
     * 在报名申请 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param vo
     *            领域视图对象。
     * @return 转换后的目标模型对象。
     */
    private Enroll convertToEntity(EnrollVO vo) {
        return Enroll.builder()
                .id(vo.getId())
                .username(vo.getUsername())
                .studentId(vo.getStudentId())
                .password(vo.getPassword())
                .email(vo.getEmail())
                .collegeId(vo.getCollegeId())
                .major(vo.getMajor())
                .gender(vo.getGender())
                .direction(vo.getDirection())
                .avatarId(vo.getAvatarFileId())
                .status(vo.getStatus())
                .introduction(vo.getIntroduction())
                .internalReferralCode(vo.getInternalReferralCode())
                .build();
    }

    /**
     * 根据学院主键获取学院名称，供报名视图组装使用。
     *
     * @param collegeId
     *            学院主键。
     * @return 查询或处理得到的报名申请 结果。
     */
    private String getCollegeName(Long collegeId) {
        if (collegeId == null) {
            return null;
        }
        College college = RepositoryObjectConverter.toDomain(collegeMapper.selectById(collegeId), College.class);
        return college != null ? college.getName() : null;
    }

    /**
     * 根据推荐码获取推荐人姓名，供报名视图组装使用。
     *
     * @param internalReferralCode
     *            内部推荐码。
     * @return 查询或处理得到的报名申请 结果。
     */
    private String getReferralUserName(String internalReferralCode) {
        if (internalReferralCode == null || internalReferralCode.trim().isEmpty()) {
            return null;
        }
        User user = RepositoryObjectConverter
                .toDomain(userMapper.selectByInternalReferralCode(internalReferralCode), User.class);
        return user != null ? user.getUsername() : null;
    }

    /**
     * 根据推荐码获取推荐人用户主键，供报名视图组装使用。
     *
     * @param internalReferralCode
     *            内部推荐码。
     * @return 查询或处理得到的报名申请 结果。
     */
    private Long getReferralUserId(String internalReferralCode) {
        if (internalReferralCode == null || internalReferralCode.trim().isEmpty()) {
            return null;
        }
        User user = RepositoryObjectConverter
                .toDomain(userMapper.selectByInternalReferralCode(internalReferralCode), User.class);
        return user != null ? user.getId() : null;
    }
}
