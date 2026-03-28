package com.bluenet.web.api.dto.college;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建学院请求DTO
 * <p>
 * 用于创建学院的请求参数
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "创建学院请求")
public class CreateCollegeRequestDTO {
    @NotBlank(message = "学院名称不能为空")
    @Size(max = 100, message = "学院名称最多100个字符")
    @Schema(description = "学院名称", required = true, example = "计算机科学与技术学院")
    private String name;
}
