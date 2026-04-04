package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.util.GradeCalculator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserConverter {

    private static final Map<Integer, String> GRADE_LABELS = Map.of(
            1,
            "大一",
            2,
            "大二",
            3,
            "大三",
            4,
            "大四");

    /**
     * 将UserVO领域值对象转换为UserInfo DTO
     *
     * @param userVO
     *            用户领域值对象
     * @return 用户信息DTO
     */
    public UserInfo convertToUserInfo(UserVO userVO) {
        Integer gradeNum = GradeCalculator.calculateGrade(userVO.getStudentId());
        String gradeLabel = gradeNum != null ? GRADE_LABELS.getOrDefault(gradeNum, gradeNum + "年级") : null;

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
                .build();
    }
}
