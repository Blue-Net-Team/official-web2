package com.bluenet.web.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Tab计数")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabCountsDTO {
    @Schema(description = "项目数")
    private Integer projects;

    @Schema(description = "个人成就数")
    private Integer achievements;

    @Schema(description = "实习数")
    private Integer internships;
}
