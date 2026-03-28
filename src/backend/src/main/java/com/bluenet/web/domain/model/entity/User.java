package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("tb_user")
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentId;
    private String email;
    private Long roleId;
    private String password;
    private String username;
    private String nickname;
    private Long collegeId;
    private String major;
    private Direction direction;
    private Gender gender;
    private String job;
    private Long avatarId;
    private Boolean disable;
    private Long qrcodeId;
    private String githubId;
    private String githubUsername;
    private String internalReferralCode;
    private String bio;
}
