package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.api.dto.college.CreateCollegeRequestDTO;
import com.bluenet.web.api.dto.college.UpdateCollegeRequestDTO;
import com.bluenet.web.application.converter.CollegeConverter;
import com.bluenet.web.application.service.CollegeService;
import com.bluenet.web.domain.exception.GlobalException;
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

        return collegeConverter.convertToDTO(loadAfterWrite(id, "创建学院失败"));
    }

    @Override
    @Transactional
    public CollegeDTO updateCollege(Long id, UpdateCollegeRequestDTO request) {
        collegeDomainService.updateCollege(id, request.getName());

        return collegeConverter.convertToDTO(loadAfterWrite(id, "更新学院失败"));
    }

    @Override
    @Transactional
    public void deleteCollege(Long id) {
        collegeDomainService.deleteCollege(id);
    }

    /**
     * 写操作后统一回读学院，避免创建和更新分支重复处理空结果。
     */
    private CollegeVO loadAfterWrite(Long id, String errorMessage) {
        Optional<CollegeVO> college = collegeDomainService.getCollegeById(id);
        if (college.isEmpty()) {
            throw new GlobalException(errorMessage);
        }
        return college.get();
    }
}
