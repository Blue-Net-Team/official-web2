package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.api.dto.college.CreateCollegeRequestDTO;
import com.bluenet.web.api.dto.college.UpdateCollegeRequestDTO;
import com.bluenet.web.application.converter.CollegeConverter;
import com.bluenet.web.application.service.CollegeService;
import com.bluenet.web.domain.model.vo.CollegeVO;
import com.bluenet.web.domain.service.CollegeDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 学院应用服务实现类
 * <p>
 * 实现学院相关的应用层服务，协调领域服务完成业务操作
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CollegeServiceImpl implements CollegeService {
    private final CollegeDomainService collegeDomainService;
    private final CollegeConverter collegeConverter;

    @Override
    public List<CollegeDTO> getAllColleges() {
        List<CollegeVO> voList = collegeDomainService.getAllColleges();
        return collegeConverter.convertToDTOList(voList);
    }

    @Override
    @Transactional
    public CollegeDTO createCollege(CreateCollegeRequestDTO request) {
        Long id = collegeDomainService.createCollege(request.getName());

        Optional<CollegeVO> created = collegeDomainService.getCollegeById(id);
        if (created.isEmpty()) {
            throw new IllegalStateException("创建学院失败");
        }

        return collegeConverter.convertToDTO(created.get());
    }

    @Override
    @Transactional
    public CollegeDTO updateCollege(Long id, UpdateCollegeRequestDTO request) {
        collegeDomainService.updateCollege(id, request.getName());

        Optional<CollegeVO> updated = collegeDomainService.getCollegeById(id);
        if (updated.isEmpty()) {
            throw new IllegalStateException("更新学院失败");
        }

        return collegeConverter.convertToDTO(updated.get());
    }

    @Override
    @Transactional
    public void deleteCollege(Long id) {
        collegeDomainService.deleteCollege(id);
    }
}
