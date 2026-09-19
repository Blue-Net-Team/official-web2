package com.bluenet.web.api.dto.learningpath;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量排序请求DTO
 * <p>
 * 用于拖拽排序后全量覆盖某方向学习步骤的展示顺序。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "学习步骤批量排序请求")
public class BatchSortRequestDTO {
    @NotEmpty(message = "排序列表不能为空")
    @Valid
    @Schema(description = "学习步骤排序项列表", required = true)
    private List<SortItemDTO> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "排序项")
    public static class SortItemDTO {
        @NotNull(message = "步骤ID不能为空")
        @Schema(description = "步骤ID", required = true)
        private Long id;

        @NotNull(message = "排序值不能为空")
        @Schema(description = "排序值（数值越小越靠前）", required = true)
        private Integer sortOrder;
    }
}
