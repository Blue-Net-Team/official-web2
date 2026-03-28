package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.vo.CollegeVO;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.service.CollegeDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 学院领域服务实现类
 * <p>
 * 实现学院相关的业务逻辑操作
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CollegeDomainServiceImpl implements CollegeDomainService {
    private final CollegeRepository collegeRepository;

    @Override
    public List<CollegeVO> getAllColleges() {
        return collegeRepository.findAll();
    }

    @Override
    public Optional<CollegeVO> getCollegeById(Long id) {
        return collegeRepository.findById(id);
    }

    @Override
    public Long createCollege(String name) {
        // 检查学院名称是否已存在
        if (collegeRepository.existsByName(name)) {
            throw new IllegalArgumentException("学院名称已存在");
        }
        return collegeRepository.save(name);
    }

    @Override
    public void updateCollege(Long id, String name) {
        // 检查学院是否存在
        if (!collegeRepository.existsById(id)) {
            throw new IllegalArgumentException("学院不存在");
        }
        // 检查学院名称是否已被其他学院使用
        if (collegeRepository.existsByNameAndIdNot(name, id)) {
            throw new IllegalArgumentException("学院名称已存在");
        }
        collegeRepository.update(id, name);
    }

    @Override
    public void deleteCollege(Long id) {
        // 检查学院是否存在
        if (!collegeRepository.existsById(id)) {
            throw new IllegalArgumentException("学院不存在");
        }
        // 检查是否有关联的用户
        if (collegeRepository.hasAssociatedUsers(id)) {
            throw new IllegalArgumentException("该学院下存在关联用户，无法删除");
        }
        // 检查是否有关联的报名记录
        if (collegeRepository.hasAssociatedEnrolls(id)) {
            throw new IllegalArgumentException("该学院下存在关联报名记录，无法删除");
        }
        collegeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return collegeRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return collegeRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        return collegeRepository.existsByNameAndIdNot(name, excludeId);
    }

    @Override
    public boolean canDelete(Long id) {
        if (!collegeRepository.existsById(id)) {
            return false;
        }
        // 没有关联用户和报名记录时可以删除
        return !collegeRepository.hasAssociatedUsers(id) && !collegeRepository.hasAssociatedEnrolls(id);
    }
}
