package com.bluenet.web.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新用户头像请求")
public class UpdateAvatarRequestDTO {

    @NotNull(message = "fileId不能为空")
    @Schema(description = "已上传的文件ID，文件类型必须为 AVATAR", example = "32")
    private Long fileId;
}
