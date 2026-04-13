package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.model.vo.CompetitionVO;

import java.util.List;
import java.util.Optional;

/**
 * 竞赛领域服务接口
 * <p>
 * 提供竞赛相关的业务逻辑操作，包括竞赛的查询、创建、更新、删除等功能
 * </p>
 */
public interface CompetitionDomainService {
    /**
     * 获取启用的竞赛列表
     *
     * @param limit
     *            限制返回数量，如果为0则返回全部
     * @return 竞赛简要信息列表
     */
    List<CompetitionBriefVO> getCompetitionList(int limit);

    /**
     * 根据ID获取竞赛详情
     *
     * @param id
     *            竞赛ID
     * @return 竞赛详细信息，如果不存在则返回Optional.empty()
     */
    Optional<CompetitionVO> getCompetitionById(Long id);

    /**
     * 创建竞赛
     *
     * @param name
     *            竞赛名称
     * @param shortName
     *            竞赛简称
     * @param logoFileId
     *            Logo文件ID
     * @param summary
     *            竞赛简介
     * @param detail
     *            竞赛详细介绍
     * @param level
     *            竞赛级别
     * @param month
     *            举办月份
     * @param organizer
     *            主办单位
     * @return 创建后的竞赛ID
     */
    Long createCompetition(String name, String shortName, Long logoFileId, String summary, String detail,
            String level, String month, String organizer);

    /**
     * 更新竞赛
     *
     * @param id
     *            竞赛ID
     * @param name
     *            竞赛名称
     * @param shortName
     *            竞赛简称
     * @param logoFileId
     *            Logo文件ID
     * @param summary
     *            竞赛简介
     * @param detail
     *            竞赛详细介绍
     * @param level
     *            竞赛级别
     * @param month
     *            举办月份
     * @param organizer
     *            主办单位
     */
    void updateCompetition(Long id, String name, String shortName, Long logoFileId, String summary, String detail,
            String level, String month, String organizer);

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
     * @param sortOrder
     *            排序号（数值越小越靠前）
     */
    void updateSortOrder(Long id, Integer sortOrder);

    /**
     * 检查竞赛是否存在
     *
     * @param id
     *            竞赛ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);

    /**
     * 更新竞赛Logo
     *
     * @param id
     *            竞赛ID
     * @param logoFileId
     *            Logo文件ID
     */
    void updateLogo(Long id, Long logoFileId);

    /**
     * 更新竞赛封面
     *
     * @param id
     *            竞赛ID
     * @param coverFileId
     *            封面文件ID
     */
    void updateCover(Long id, Long coverFileId);
}
