package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bug 报告图片 Mapper 专用数据对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_bug_report_image")
public class BugReportImageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bugReportId;
    private Long fileId;
}
