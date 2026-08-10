package com.bluenet.web.api.dto.achievement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "成就关联的系统内成员")
public class AchievementMemberDTO {
    @Schema(description = "成员用户ID", example = "123")
    private Long userId;

    @Schema(description = "成员姓名", example = "张三")
    private String username;

    @Schema(description = "头像文件ID", example = "456")
    private Long avatarFileId;
}
