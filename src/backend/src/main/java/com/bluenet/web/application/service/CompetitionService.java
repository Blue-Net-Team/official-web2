package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.competition.*;

import java.util.List;

/**
 * 竞赛应用服务接口
 * <p>
 * 提供竞赛相关的应用层服务，协调领域服务完成业务操作， 负责VO与DTO之间的转换
 * </p>
 */
public interface CompetitionService {
    /**
     * 获取竞赛响应列表（包含封面信息）
     *
     * @param limit
     *            限制返回数量，如果为0则返回全部
     * @return 竞赛响应DTO列表
     */
    List<CompetitionResponseDTO> getCompetitionResponseList(int limit);

    /**
     * 分页查询竞赛列表
     *
     * @param page
     *            页码（从0开始），为null时默认0
     * @param size
     *            每页数量，为null时默认10，最大50
     * @return 分页竞赛响应DTO
     */
    PageDTO<CompetitionResponseDTO> getCompetitionPage(Integer page, Integer size);

    /**
     * 创建竞赛
     *
     * @param request
     *            创建请求DTO
     * @return 创建后的竞赛响应DTO
     */
    CompetitionResponseDTO createCompetition(CompetitionRequestDTO request);

    /**
     * 更新竞赛
     *
     * @param id
     *            竞赛ID
     * @param request
     *            更新请求DTO
     * @return 更新后的竞赛响应DTO
     */
    CompetitionResponseDTO updateCompetition(Long id, CompetitionRequestDTO request);

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
     * @param id
     *            竞赛ID
     * @param request
     *            排序更新请求DTO
     */
    void updateSortOrder(Long id, UpdateSortOrderRequestDTO request);

    /**
     * 批量更新竞赛排序号
     *
     * @param request
     *            批量排序请求DTO
     */
    void batchUpdateSortOrder(BatchSortRequestDTO request);

    /**
     * 移动竞赛排序（上移或下移一位）
     *
     * @param id
     *            竞赛ID
     * @param request
     *            移动请求DTO
     */
    void moveCompetition(Long id, MoveCompetitionRequestDTO request);
}
