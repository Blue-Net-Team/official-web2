package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.competition.BatchSortRequestDTO;
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.application.CompetitionResult;
import com.bluenet.web.application.command.competition.CompetitionCommands;

import java.util.List;

/**
 * 竞赛应用服务接口。
 * <p>
 * 定义了竞赛聚合在应用层的所有业务操作。
 * </p>
 */
public interface CompetitionAppService {

    /**
     * 获取竞赛响应列表
     *
     * @param limit
     *            限制返回数量
     * @return 竞赛响应DTO列表
     */
    List<CompetitionResponseDTO> getCompetitionResponseList(int limit);

    /**
     * 分页查询竞赛列表
     *
     * @param page
     *            页码
     * @param size
     *            每页数量
     * @return 分页竞赛响应DTO
     */
    PageDTO<CompetitionResponseDTO> getCompetitionPage(Integer page, Integer size);

    /**
     * 创建竞赛
     *
     * @param command
     *            创建命令
     * @return 创建后的竞赛结果
     */
    CompetitionResult createCompetition(CompetitionCommands.CreateCompetitionCommand command);

    /**
     * 更新竞赛
     *
     * @param command
     *            更新命令
     * @return 更新后的竞赛结果
     */
    CompetitionResult updateCompetition(CompetitionCommands.UpdateCompetitionCommand command);

    /**
     * 删除竞赛
     *
     * @param id
     *            竞赛ID
     */
    void deleteCompetition(Long id);

    /**
     * 更新竞赛排序号
     *
     * @param command
     *            排序更新命令
     */
    void updateSortOrder(CompetitionCommands.UpdateSortOrderCommand command);

    /**
     * 批量更新竞赛排序号
     *
     * @param request
     *            批量排序请求DTO
     */
    void batchUpdateSortOrder(BatchSortRequestDTO request);

    /**
     * 移动竞赛排序
     *
     * @param command
     *            移动命令
     */
    void moveCompetition(CompetitionCommands.MoveCompetitionCommand command);
}
