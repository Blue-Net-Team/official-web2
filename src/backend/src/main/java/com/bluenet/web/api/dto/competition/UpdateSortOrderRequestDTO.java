package com.bluenet.web.api.dto.competition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新排序请求")
public class UpdateSortOrderRequestDTO {
    @NotNull(message = "排序权重不能为空")
    @Schema(description = "排序权重", required = true)
    private Integer sortOrder;
}
