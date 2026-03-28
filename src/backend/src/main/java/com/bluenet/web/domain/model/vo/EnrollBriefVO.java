package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EnrollBriefVO {
    private Long id;
    private String username;
    private String studentId;
    private String email;
    private String collegeName;
    private String major;
    private Integer grade;
    private Direction direction;
    private EnrollStatus status;
    private Long avatarFileId;
}
