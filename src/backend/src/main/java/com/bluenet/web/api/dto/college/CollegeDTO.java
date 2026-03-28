package com.bluenet.web.api.dto.college;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学院数据传输对象
 * <p>
 * 用于API响应中返回学院信息
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "学院信息")
public class CollegeDTO {
    @Schema(description = "学院ID")
    private Long id;

    @Schema(description = "学院名称")
    private String name;
}
