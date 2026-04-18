package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("tb_enroll")
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Enroll {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String studentId;
    private String password;
    private String internalReferralCode;
    private Long collegeId;
    private String major;
    private Gender gender;
    private Direction direction;
    private Long avatarId;
    private EnrollStatus status;
    private String email;
    private String introduction;
}
