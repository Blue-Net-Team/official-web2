package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bug 报告 Mapper 专用数据对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_bug_report")
public class BugReportDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String description;
    private String pageUrl;
    private String environmentJson;
    private String reporterEmail;
    private BugReportStatus status;
    private String githubIssueUrl;
    private Integer githubIssueNumber;
}
