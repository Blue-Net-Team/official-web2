package com.bluenet.web.api.dto.enrollment;

import com.bluenet.web.domain.model.enumerate.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "发起报名请求")
public class CreateEnrollmentRequestDTO {
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名最多50个字符")
    @Schema(description = "真实姓名", required = true, example = "张三")
    private String username;

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^\\d{12,13}$", message = "学号必须为12-13位数字")
    @Schema(description = "学号，12-13位数字", required = true, example = "20210001001")
    private String studentId;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最多100个字符")
    @Schema(description = "邮箱，用于接收通知", required = true, example = "zhangsan@example.com")
    private String email;

    @NotNull(message = "学院ID不能为空")
    @Schema(description = "学院ID", required = true, example = "1")
    private Long collegeId;

    @NotBlank(message = "专业不能为空")
    @Size(max = 100, message = "专业最多100个字符")
    @Schema(description = "专业", required = true, example = "计算机科学与技术")
    private String major;

    @NotNull(message = "年级不能为空")
    @Min(value = 1, message = "年级最小为1")
    @Max(value = 6, message = "年级最大为6")
    @Schema(description = "年级，1-6", required = true, example = "2")
    private Integer grade;

    @NotNull(message = "方向不能为空")
    @Schema(description = "报名方向", required = true, example = "computer_vision")
    private Direction direction;

    @Schema(description = "头像文件ID（需先调用文件上传接口获取）", example = "123")
    private Long avatarId;

    @NotBlank(message = "自我介绍不能为空")
    @Size(min = 100, max = 500, message = "自我介绍需要100-500字")
    @Schema(description = "自我介绍，100-500字", required = true, example = "我是xxx，来自xxx专业...")
    private String introduction;

    @Pattern(regexp = "^[A-Z0-9]{8}$", message = "内推码必须为8位大写字母或数字")
    @Schema(description = "内推码，8位大写字母+数字", example = "ABC12345")
    private String internalReferralCode;

    @Schema(description = "是否强制更新已有报名，默认false", example = "false")
    private Boolean forceUpdate;
}
