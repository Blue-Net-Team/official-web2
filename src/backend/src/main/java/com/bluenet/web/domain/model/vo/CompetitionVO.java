package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionVO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 业务对象名称。
     */
    private String name;
    /**
     * 业务对象简称，用于紧凑展示。
     */
    private String shortName;
    /**
     * Logo 图片访问地址。
     */
    private String logoUrl;
    /**
     * Logo 图片对应的文件记录标识。
     */
    private Long logoFileId;
    /**
     * 封面图对应的文件记录标识。
     */
    private Long coverFileId;
    /**
     * 项目、竞赛或经历的摘要说明。
     */
    private String summary;
    /**
     * 成果、权限或展示项的层级。
     */
    private String level;
    /**
     * 统计数据对应的月份。
     */
    private String month;
    /**
     * 竞赛或活动主办方。
     */
    private String organizer;
    /**
     * 列表展示排序值，数值越大通常越靠前。
     */
    private Integer sortOrder;
}
