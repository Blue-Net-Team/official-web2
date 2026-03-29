package com.bluenet.web.api.dto.equipment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备响应DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "设备信息")
public class EquipmentDTO {
    @Schema(description = "设备ID")
    private Long id;

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "设备品牌")
    private String brand;

    @Schema(description = "设备描述")
    private String description;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "图片文件ID")
    private Long imageFileId;
}
