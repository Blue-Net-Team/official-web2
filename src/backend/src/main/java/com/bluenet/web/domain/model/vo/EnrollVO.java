package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class EnrollVO {
    private Long id;
    private String username;
    private String studentId;
    private Long collegeId;
    private String collegeName;
    private String major;
    private Gender gender;
    private Direction direction;
    private Long avatarFileId;
    private EnrollStatus status;
    private String internalReferralCode;
    private Long referralUserId;
    private String referralUserName;
    private String email;
    private String introduction;
    private String password;
}
