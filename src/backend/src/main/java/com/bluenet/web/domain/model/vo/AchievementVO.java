package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class AchievementVO {
    private Long id;
    private String title;
    private String relateTo;
    private AchievementType type;
    private LocalDate achieveAt;
    private AwardLevel awardLevel;
    private String awardName;
    private String competitionName;
    private String competitionShortName;
    private Long competitionLogoFileId;
    private Long fileId;
    private String fileUrl;
}
