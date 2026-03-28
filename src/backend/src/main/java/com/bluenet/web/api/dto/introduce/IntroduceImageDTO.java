package com.bluenet.web.api.dto.introduce;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 介绍图片DTO
 * <p>
 * 用于对外暴露的介绍图片数据传输对象。
 * </p>
 */
@Schema(description = "介绍图片信息")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntroduceImageDTO {
    @Schema(description = "图片ID")
    private Long id;

    @Schema(description = "图片类型", example = "laboratory", allowableValues = { "laboratory", "equipment", "team_photo",
            "direction", "competition", "patent", "paper" })
    private ImageType type;

    @Schema(description = "图片描述")
    private String description;

    @Schema(description = "关联的文件ID")
    private Long fileId;

    @Schema(description = "方向（仅在 type=direction 时有效）", allowableValues = { "COMPUTER_VISION", "STRUCTURAL_DESIGN",
            "EMBEDDED" })
    private Direction direction;

    @Schema(description = "文件URL（用于前端直接访问）")
    private String fileUrl;
}
