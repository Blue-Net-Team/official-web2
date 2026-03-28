package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CompetitionVO {
    private Long id;
    private String name;
    private String shortName;
    private String logoUrl;
    private Long logoFileId;
    private String summary;
    private String detail;
    private Integer sortOrder;
    private Boolean enabled;
}
