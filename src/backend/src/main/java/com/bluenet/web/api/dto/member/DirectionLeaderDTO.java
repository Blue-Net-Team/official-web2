package com.bluenet.web.api.dto.member;

import com.bluenet.web.domain.model.enumerate.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "方向负责人信息")
public class DirectionLeaderDTO {
    @Schema(description = "方向", example = "computer_vision")
    private Direction direction;

    @Schema(description = "方向名称", example = "计算机视觉")
    private String directionName;

    @Schema(description = "负责人信息，若无则为null")
    private LeaderInfo leader;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "负责人基本信息")
    public static class LeaderInfo {
        @Schema(description = "成员ID", example = "1")
        private Long id;

        @Schema(description = "真实姓名", example = "李四")
        private String username;

        @Schema(description = "昵称", example = "视觉组长")
        private String nickname;

        @Schema(description = "头像文件ID", example = "100")
        private Long avatarFileId;
    }
}
