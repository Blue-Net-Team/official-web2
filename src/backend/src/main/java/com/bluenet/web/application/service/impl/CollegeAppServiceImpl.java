package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.CollegeResult;
import com.bluenet.web.application.command.college.CollegeCommands;
import com.bluenet.web.application.service.CollegeAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学院应用服务实现。
 * <p>
 * 实现学院聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CollegeAppServiceImpl implements CollegeAppService {
    private final CollegeRepository collegeRepository;

    /**
     * 查询学院列表。
     *
     * @return 学院结果列表
     */
    @Override
    public List<CollegeResult> getAllColleges() {
        return collegeRepository.findAll()
                .stream()
                .map(this::toResult)
                .toList();
    }

    /**
     * 创建学院。
     *
     * @param command
     *            创建学院命令
     * @return 创建后的学院结果
     */
    @Override
    @Transactional
    public CollegeResult createCollege(CollegeCommands.CreateCollegeCommand command) {
        if (collegeRepository.existsByName(command.name())) {
            throw new IllegalArgumentException("学院名称已存在");
        }
        College college = College.create(command.name());
        collegeRepository.save(college);
        return toResult(college);
    }

    /**
     * 更新学院。
     *
     * @param command
     *            更新学院命令
     * @return 更新后的学院结果
     */
    @Override
    @Transactional
    public CollegeResult updateCollege(CollegeCommands.UpdateCollegeCommand command) {
        College college = collegeRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("学院不存在"));
        if (collegeRepository.existsByNameAndIdNot(command.name(), command.id())) {
            throw new IllegalArgumentException("学院名称已存在");
        }
        college.rename(command.name());
        collegeRepository.update(college);
        return toResult(college);
    }

    /**
     * 删除学院。
     *
     * @param id
     *            学院ID
     */
    @Override
    @Transactional
    public void deleteCollege(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("学院不存在"));
        if (collegeRepository.hasAssociatedUsers(id)) {
            throw new IllegalArgumentException("该学院下存在关联用户，无法删除");
        }
        if (collegeRepository.hasAssociatedEnrolls(id)) {
            throw new IllegalArgumentException("该学院下存在关联报名记录，无法删除");
        }
        collegeRepository.deleteById(id);
    }

    private CollegeResult toResult(College college) {
        return new CollegeResult(college.getId(), college.getName());
    }
}
