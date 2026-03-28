package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {
    private final UserRepository userRepository;

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
}
