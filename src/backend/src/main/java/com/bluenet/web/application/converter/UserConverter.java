package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.util.GradeCalculator;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    /**
     * 将UserVO领域值对象转换为UserInfo DTO
     *
     * @param userVO
     *            用户领域值对象
     * @return 用户信息DTO
     */
    public UserInfo convertToUserInfo(UserVO userVO) {
        String gradeLabel = GradeCalculator.getGradeLabel(userVO.getStudentId(), userVO.getAssessmentGradeYear());

        return UserInfo.builder()
                .id(userVO.getId())
                .username(userVO.getUsername())
                .nickname(userVO.getNickname())
                .email(userVO.getEmail())
                .college(userVO.getCollege())
                .major(userVO.getMajor())
                .grade(gradeLabel)
                .direction(userVO.getDirection())
                .gender(userVO.getGender())
                .avatarFileId(userVO.getAvatarFileId())
                .roleName(userVO.getRoleName())
                .bio(userVO.getBio())
                .githubUsername(userVO.getGithubUsername())
                .build();
    }
}
