package com.bluenet.web.api.dto.enrollment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Approve enrollment request")
public class ApproveEnrollmentRequestDTO {
    @Min(value = 2000, message = "assessmentGradeYear must be between 2000 and 2100")
    @Max(value = 2100, message = "assessmentGradeYear must be between 2000 and 2100")
    @Schema(description = "Assessment grade year override, for example 2024", example = "2024")
    private Integer assessmentGradeYear;
}
