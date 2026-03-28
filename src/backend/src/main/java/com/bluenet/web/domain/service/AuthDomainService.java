package com.bluenet.web.domain.service;

import java.util.Optional;

import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.vo.UserVO;

public interface AuthDomainService {

    /**
     * 检查本地登录是否合法
     *
     * @param userSign
     *            用户标识（如学号或邮箱）
     * @param principal
     *            认证凭证（如用户密码）
     * @param localLoginType
     *            本地登录类型
     * @return 如果合法，返回对应的用户信息；否则返回空
     */
    Optional<UserVO> checkLocalValid(String userSign, String principal, LocalLoginType localLoginType);
}
