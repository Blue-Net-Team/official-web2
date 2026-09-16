package com.bluenet.web.api.dto.aitrace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分布计数项")
public class AiCountItemDTO {
    @Schema(description = "计数维度名称")
    private String label;
    @Schema(description = "次数")
    private Long count;
    @Schema(description = "占比（0~1）")
    private Double ratio;
}
