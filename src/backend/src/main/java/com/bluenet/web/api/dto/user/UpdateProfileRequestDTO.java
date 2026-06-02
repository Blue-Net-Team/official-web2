package com.bluenet.web.api.dto.user;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "更新用户画像请求")
@Data
public class UpdateProfileRequestDTO {
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "方向")
    private Direction direction;

    @Schema(description = "性别")
    private Gender gender;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "微信二维码文件ID")
    private Long qrcodeFileId;
}
