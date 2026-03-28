package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CompetitionBriefVO {
    private Long id;
    private String name;
    private String shortName;
    private String logoUrl;
    private Long logoFileId;
    private String summary;
}
