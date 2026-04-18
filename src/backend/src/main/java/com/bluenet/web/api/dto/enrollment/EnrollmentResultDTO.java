package com.bluenet.web.api.dto.enrollment;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
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
    private String email;
    private String collegeName;
    private String major;
    private Gender gender;
    private Direction direction;
    private EnrollStatus status;
    private Long avatarFileId;
    private boolean created;
}
