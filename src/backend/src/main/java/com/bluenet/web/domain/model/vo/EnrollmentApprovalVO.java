package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import lombok.Builder;
import lombok.Data;

/**
 * 报名审批领域结果。
 *
 * <p>
 * 领域层只返回通知所需的事实数据，具体消息分发由应用层编排。
 * </p>
 */
@Data
@Builder
public class EnrollmentApprovalVO {
    private Long id;
    private EnrollStatus status;
    private Long userId;
    private boolean newUserCreated;
    private String username;
    private String studentId;
    private String email;
    private String initialPassword;
}
