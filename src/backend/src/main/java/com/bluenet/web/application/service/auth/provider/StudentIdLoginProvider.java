package com.bluenet.web.application.service.auth.provider;

import java.util.Optional;

import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.application.service.auth.strategy.AbstractAuthProvider;
import com.bluenet.web.application.service.auth.strategy.AuthProviderType;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.AuthDomainService;

import lombok.extern.slf4j.Slf4j;

/**
 * 学号密码登录凭证校验 provider。
 */
@Slf4j
public class StudentIdLoginProvider extends AbstractAuthProvider<StudentIdLoginRequestDTO, UserVO> {
    private final AuthDomainService authDomainService;

    public StudentIdLoginProvider(AuthDomainService authDomainService) {
        super(AuthProviderType.STUDENT_ID);
        this.authDomainService = authDomainService;
    }

    /**
     * 校验学号密码并返回用户。
     *
     * @param request
     *            登录请求。
     * @return 通过校验的用户。
     */
    public UserVO authenticate(StudentIdLoginRequestDTO request) {
        Optional<UserVO> userVOOptional = authDomainService.checkLocalValid(
                request.getStudentId(),
                request.getPassword(),
                LocalLoginType.STUDENT_ID);
        return userVOOptional.orElseThrow(() -> {
            log.warn("Login failed: invalid credentials - {}", request.getStudentId());
            return new Unauthorized("学号或密码错误");
        });
    }
}
