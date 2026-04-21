package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EnrollBriefVO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 用户真实姓名或登录用户名。
     */
    private String username;
    /**
     * 学生学号。
     */
    private String studentId;
    /**
     * 用户邮箱地址。
     */
    private String email;
    /**
     * 学院名称。
     */
    private String collegeName;
    /**
     * 用户所在专业。
     */
    private String major;
    /**
     * 用户性别。
     */
    private Gender gender;
    /**
     * 用户或考核所属技术方向。
     */
    private Direction direction;
    /**
     * 当前业务流程、任务或记录的状态。
     */
    private EnrollStatus status;
    /**
     * 用户头像对应的文件记录标识。
     */
    private Long avatarFileId;
}
