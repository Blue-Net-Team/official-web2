package com.bluenet.web.api.converter.enroll;

import com.bluenet.web.api.dto.enrollment.ApproveEnrollmentRequestDTO;
import com.bluenet.web.api.dto.enrollment.CreateEnrollmentRequestDTO;
import com.bluenet.web.api.dto.enrollment.RejectEnrollmentRequestDTO;
import com.bluenet.web.application.command.enroll.EnrollCommands;
import org.springframework.stereotype.Component;

/**
 * 报名请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class EnrollRequestConverter {

    /**
     * 将创建请求 DTO 转换为命令
     */
    public EnrollCommands.CreateEnrollmentCommand toCommand(CreateEnrollmentRequestDTO dto) {
        return new EnrollCommands.CreateEnrollmentCommand(
                dto.getUsername(),
                dto.getStudentId(),
                dto.getEmail(),
                dto.getCollegeId(),
                dto.getMajor(),
                dto.getGender(),
                dto.getDirection(),
                dto.getAvatarId(),
                dto.getIntroduction(),
                dto.getInternalReferralCode(),
                dto.getForceUpdate());
    }

    /**
     * 将更新请求 DTO 转换为命令（按学号更新）
     */
    public EnrollCommands.UpdateEnrollmentCommand toUpdateCommand(String studentId, CreateEnrollmentRequestDTO dto) {
        return new EnrollCommands.UpdateEnrollmentCommand(
                studentId,
                dto.getUsername(),
                dto.getEmail(),
                dto.getCollegeId(),
                dto.getMajor(),
                dto.getGender(),
                dto.getDirection(),
                dto.getAvatarId(),
                dto.getIntroduction(),
                dto.getInternalReferralCode());
    }

    /**
     * 将查询参数转换为列表查询命令
     */
    public EnrollCommands.GetEnrollmentListCommand toListCommand(Integer page, Integer size,
            String keyword, String status, String direction) {
        return new EnrollCommands.GetEnrollmentListCommand(
                page,
                size,
                keyword,
                status != null
                        ? com.bluenet.web.domain.model.enumerate.EnrollStatus.valueOf(status.toUpperCase())
                        : null,
                direction != null
                        ? com.bluenet.web.domain.model.enumerate.Direction.valueOf(direction.toUpperCase())
                        : null);
    }

    /**
     * 将审核请求 DTO 转换为命令
     */
    public EnrollCommands.ApproveEnrollmentCommand toCommand(ApproveEnrollmentRequestDTO dto) {
        if (dto == null) {
            return new EnrollCommands.ApproveEnrollmentCommand(null);
        }
        return new EnrollCommands.ApproveEnrollmentCommand(dto.getAssessmentGradeYear());
    }

    /**
     * 将拒绝请求 DTO 转换为命令
     */
    public EnrollCommands.RejectEnrollmentCommand toCommand(RejectEnrollmentRequestDTO dto) {
        if (dto == null) {
            return new EnrollCommands.RejectEnrollmentCommand(null);
        }
        return new EnrollCommands.RejectEnrollmentCommand(dto.getReason());
    }
}
