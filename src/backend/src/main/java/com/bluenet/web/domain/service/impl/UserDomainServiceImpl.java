package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void updateUserAvatar(Long userId, FileVO file) {
        UserVO user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFound("用户不存在"));
        userRepository.updateAvatar(user, file);
    }

    @Override
    public Optional<UserVO> getUser(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, String username, String nickname, String college,
            String major, Direction direction, Gender gender, String bio) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFound("用户不存在"));

        userRepository.updateProfile(userId, username, nickname, college, major, direction, gender, bio);
    }

    @Override
    public TabCountsVO getTabCounts(Long userId) {
        return userRepository.getTabCounts(userId);
    }

    @Override
    @Transactional
    public void changeEmail(Long userId, String currentEmail, String originalEmailVerifyCode,
            String newEmail, String newEmailVerifyCode) {
        UserVO user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFound("用户不存在"));

        if (!user.getEmail().equals(currentEmail)) {
            throw new BadRequest("当前邮箱已变更，请刷新页面重试");
        }

        verifyCode(currentEmail, originalEmailVerifyCode, "change-email-original");
        verifyCode(newEmail, newEmailVerifyCode, "change-email-new");

        Optional<UserVO> existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent()) {
            throw new BadRequest("该邮箱已被其他账号绑定");
        }

        userRepository.updateEmail(userId, newEmail);

        verificationCodeRepository.markAsUsed(currentEmail, originalEmailVerifyCode, "change-email-original");
        verificationCodeRepository.markAsUsed(newEmail, newEmailVerifyCode, "change-email-new");
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String rawNewPassword) {
        UserVO userVO = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFound("用户不存在"));
        User user = User.reconstruct(userVO.getId(), userVO.getPassword());
        user.changePassword(passwordEncoder.encode(rawNewPassword));
        userRepository.updatePassword(user.getId(), user.getPassword());
    }

    private void verifyCode(String email, String code, String scene) {
        Optional<VerifyCodeVO> verifyCodeVO = verificationCodeRepository.findByEmailAndCodeAndScene(email, code, scene);
        if (verifyCodeVO.isEmpty()) {
            throw new BadRequest("验证码错误");
        }
        if (verifyCodeVO.get().isExpired()) {
            throw new BadRequest("验证码已过期");
        }
        if (verifyCodeVO.get().isUsed()) {
            throw new BadRequest("验证码已被使用");
        }
    }
}
