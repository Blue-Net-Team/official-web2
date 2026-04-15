package com.bluenet.web.api.dto.competition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "批量排序请求")
public class BatchSortRequestDTO {
    @NotEmpty(message = "排序列表不能为空")
    @Valid
    @Schema(description = "竞赛排序项列表", required = true)
    private List<SortItemDTO> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "排序项")
    public static class SortItemDTO {
        @NotNull(message = "竞赛ID不能为空")
        @Schema(description = "竞赛ID", required = true)
        private Long id;

        @NotNull(message = "排序号不能为空")
        @Schema(description = "排序号（数值越小越靠前）", required = true)
        private Integer sortOrder;
    }
}
