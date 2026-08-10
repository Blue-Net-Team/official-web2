package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.vo.UserOnboardingCreateUserRequest;
import com.bluenet.web.domain.model.vo.UserOnboardingResult;

/**
 * 用户入职领域服务。
 * <p>
 * 封装"创建新用户并发放初始凭据"的完整流程， 供 WPS 表单和报名审批等多个入口复用。
 * </p>
 */
public interface UserOnboardingService {

    /**
     * 创建用户，系统自动生成初始密码。
     *
     * @param request
     *            创建用户请求
     * @return 创建结果（含生成的初始密码）
     */
    UserOnboardingResult createUserWithGeneratedPassword(UserOnboardingCreateUserRequest request);

    /**
     * 创建用户，使用外部提供的初始密码。
     *
     * @param request
     *            创建用户请求
     * @param initialPassword
     *            初始密码（明文）
     * @return 创建结果
     */
    UserOnboardingResult createUser(UserOnboardingCreateUserRequest request, String initialPassword);

    /**
     * 查找 MEMBER 角色，失败时抛出异常。
     *
     * @return MEMBER 角色实体
     */
    Role getMemberRole();

    /**
     * 查找 CANDIDATE 角色，失败时抛出异常。
     *
     * @return CANDIDATE 角色实体
     */
    Role getCandidateRole();
}
