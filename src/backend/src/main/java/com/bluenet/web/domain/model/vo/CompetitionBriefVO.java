package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionBriefVO {
    private Long id;
    private String name;
    private String shortName;
    private String logoUrl;
    private Long logoFileId;
    private Long coverFileId;
    private String summary;
    private String level;
    private String month;
    private String organizer;
}
