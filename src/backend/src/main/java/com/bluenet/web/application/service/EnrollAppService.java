package com.bluenet.web.application.service;

import com.bluenet.web.application.result.enroll.EnrollResult;
import com.bluenet.web.application.command.enroll.EnrollCommands;
import com.bluenet.web.application.query.enroll.GetEnrollmentListQuery;
import org.springframework.data.domain.Page;

/**
 * 报名应用服务接口。
 * <p>
 * 定义了报名聚合在应用层的所有业务操作。
 * </p>
 */
public interface EnrollAppService {

    /**
     * 创建报名
     *
     * @param command
     *            创建命令
     * @return 创建后的报名结果
     */
    EnrollResult.Enrollment createEnrollment(EnrollCommands.CreateEnrollmentCommand command);

    /**
     * 更新报名
     *
     * @param command
     *            更新命令
     * @return 更新后的报名结果
     */
    EnrollResult.Enrollment updateEnrollment(EnrollCommands.UpdateEnrollmentCommand command);

    /**
     * 分页查询报名列表
     *
     * @param query
     *            查询参数
     * @return 分页后的报名摘要结果
     */
    Page<EnrollResult.Brief> getEnrollmentList(GetEnrollmentListQuery query);

    /**
     * 获取报名详情
     *
     * @param id
     *            报名ID
     * @return 报名详情结果
     */
    EnrollResult.Detail getEnrollmentDetail(Long id);

    /**
     * 审核通过报名
     *
     * @param id
     *            报名ID
     * @return 审核结果
     */
    EnrollResult.Approval approveEnrollment(Long id);

    /**
     * 审核通过报名（带考核年级）
     *
     * @param id
     *            报名ID
     * @param command
     *            审核命令
     * @return 审核结果
     */
    EnrollResult.Approval approveEnrollment(Long id, EnrollCommands.ApproveEnrollmentCommand command);

    /**
     * 拒绝报名
     *
     * @param id
     *            报名ID
     * @param command
     *            拒绝命令
     * @return 审核结果
     */
    EnrollResult.Approval rejectEnrollment(Long id, EnrollCommands.RejectEnrollmentCommand command);

    /**
     * 获取报名统计
     *
     * @return 统计数据
     */
    EnrollResult.Statistics getStatistics();
}
