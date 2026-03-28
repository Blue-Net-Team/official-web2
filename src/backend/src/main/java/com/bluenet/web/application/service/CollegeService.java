package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.api.dto.college.CreateCollegeRequestDTO;
import com.bluenet.web.api.dto.college.UpdateCollegeRequestDTO;

import java.util.List;

/**
 * 学院应用服务接口
 * <p>
 * 提供学院相关的应用层服务，协调领域服务完成业务操作， 负责VO与DTO之间的转换
 * </p>
 */
public interface CollegeService {
    /**
     * 获取所有学院列表
     *
     * @return 学院DTO列表
     */
    List<CollegeDTO> getAllColleges();

    /**
     * 创建学院
     *
     * @param request
     *            创建请求DTO
     * @return 创建后的学院DTO
     */
    CollegeDTO createCollege(CreateCollegeRequestDTO request);

    /**
     * 更新学院
     *
     * @param id
     *            学院ID
     * @param request
     *            更新请求DTO
     * @return 更新后的学院DTO
     */
    CollegeDTO updateCollege(Long id, UpdateCollegeRequestDTO request);

    /**
     * 删除学院
     *
     * @param id
     *            学院ID
     */
    void deleteCollege(Long id);
}
