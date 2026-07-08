package com.bluenet.web.application.service;

import com.bluenet.web.application.result.competition.CompetitionResult;
import com.bluenet.web.application.command.competition.CompetitionCommands;
import com.bluenet.web.domain.model.readmodel.CompetitionReadModel;

import org.springframework.data.domain.Page;

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
     * @return 竞赛VO列表
     */
    List<CompetitionReadModel> getCompetitionResponseList(int limit);

    /**
     * 分页查询竞赛列表
     *
     * @param page
     *            页码
     * @param size
     *            每页数量
     * @return 分页竞赛VO
     */
    Page<CompetitionReadModel> getCompetitionPage(Integer page, Integer size);

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
     * @param command
     *            批量排序命令
     */
    void batchUpdateSortOrder(CompetitionCommands.BatchUpdateSortOrderCommand command);

    /**
     * 移动竞赛排序
     *
     * @param command
     *            移动命令
     */
    void moveCompetition(CompetitionCommands.MoveCompetitionCommand command);
}
