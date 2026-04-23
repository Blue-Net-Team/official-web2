package com.bluenet.web.application.service;

import com.bluenet.web.application.CollegeResult;
import com.bluenet.web.application.command.college.CollegeCommands;

import java.util.List;

/**
 * 学院应用服务接口。
 * <p>
 * 定义了学院聚合在应用层的所有业务操作。
 * </p>
 */
public interface CollegeAppService {
    /**
     * 获取所有学院列表
     *
     * @return 学院结果列表
     */
    List<CollegeResult> getAllColleges();

    /**
     * 创建学院
     *
     * @param command
     *            创建命令
     * @return 创建后的学院结果
     */
    CollegeResult createCollege(CollegeCommands.CreateCollegeCommand command);

    /**
     * 更新学院
     *
     * @param command
     *            更新命令
     * @return 更新后的学院结果
     */
    CollegeResult updateCollege(CollegeCommands.UpdateCollegeCommand command);

    /**
     * 删除学院
     *
     * @param id
     *            学院ID
     */
    void deleteCollege(Long id);
}
