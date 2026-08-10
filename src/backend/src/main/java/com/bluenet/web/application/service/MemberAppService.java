package com.bluenet.web.application.service;

import com.bluenet.web.application.result.achievement.AchievementResult;
import com.bluenet.web.application.result.member.MemberResult;
import com.bluenet.web.application.result.user.UserExperienceResult;
import com.bluenet.web.application.query.member.GetMemberListQuery;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 成员应用服务接口。
 * <p>
 * 定义了成员聚合在应用层的所有业务操作。
 * </p>
 */
public interface MemberAppService {
    /**
     * 获取团队成员列表
     *
     * @param query
     *            查询参数
     * @return 分页成员结果
     */
    Page<MemberResult> getMemberList(GetMemberListQuery query);

    /**
     * 获取成员详情
     *
     * @param id
     *            成员ID
     * @return 成员结果
     */
    MemberResult getMemberById(Long id);

    /**
     * 获取方向负责人
     *
     * @return 方向负责人结果列表
     */
    List<MemberResult> getDirectionLeaders();

    /**
     * 获取成员经历
     *
     * @param memberId
     *            成员ID
     * @param type
     *            经历类型
     * @return 经历结果列表
     */
    List<UserExperienceResult> getMemberExperiences(Long memberId, String type);

    /**
     * 获取成员关联的官方成就列表，按获奖日期倒序。
     *
     * @param memberId
     *            成员ID
     * @return 成就结果列表
     */
    List<AchievementResult> getMemberAchievements(Long memberId);
}
