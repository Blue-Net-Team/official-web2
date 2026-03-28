package com.bluenet.web.api.dto.enrollment;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollmentResultDTO {
    private Long id;
    private String username;
    private String studentId;
    private Direction direction;
    private EnrollStatus status;
    private boolean created;
}
