package com.bluenet.web.api.dto.equipment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新设备请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新设备请求")
public class UpdateEquipmentRequestDTO {
    @Size(max = 100, message = "设备名称不能超过100个字符")
    @Schema(description = "设备名称", example = "3D打印机")
    private String name;

    @Size(max = 100, message = "品牌不能超过100个字符")
    @Schema(description = "设备品牌", example = "泰尔时代")
    private String brand;

    @Schema(description = "设备描述", example = "高精度FDM 3D打印设备...")
    private String description;

    @Schema(description = "图片文件ID")
    private Long imageFileId;

    @Schema(description = "排序权重，越大越靠前", example = "0")
    private Integer sortOrder;
}
