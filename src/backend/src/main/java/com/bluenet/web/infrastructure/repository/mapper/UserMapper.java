package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User selectByEmail(String email);

    User selectByStudentId(String studentId);

    User selectByInternalReferralCode(String code);

    int updateAvatarId(Long id, Long avatarId);

    int updateQrcodeId(Long id, Long qrcodeId);

    IPage<User> selectByRoleNamesAndDirection(
            Page<User> page,
            @Param("roleNames") List<String> roleNames,
            @Param("direction") Direction direction,
            @Param("requireDirectionNotNull") Boolean requireDirectionNotNull,
            @Param("excludeUsername") String excludeUsername);

    int updateProfile(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("nickname") String nickname,
            @Param("college") String college,
            @Param("major") String major,
            @Param("direction") Direction direction,
            @Param("gender") Gender gender,
            @Param("bio") String bio);
}
