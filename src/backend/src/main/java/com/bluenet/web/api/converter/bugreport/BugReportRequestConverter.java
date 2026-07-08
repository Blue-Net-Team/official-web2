package com.bluenet.web.api.converter.bugreport;

import com.bluenet.web.api.dto.bugreport.BugReportListQueryDTO;
import com.bluenet.web.api.dto.bugreport.CreateBugReportRequestDTO;
import com.bluenet.web.application.command.bugreport.BugReportCommands;
import com.bluenet.web.application.query.bugreport.GetBugReportListQuery;
import org.springframework.stereotype.Component;

/**
 * Bug 报告请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command/Query
 * </p>
 */
@Component
public class BugReportRequestConverter {

    /**
     * 将创建请求 DTO 转换为命令
     */
    public BugReportCommands.CreateBugReportCommand toCommand(CreateBugReportRequestDTO dto) {
        return new BugReportCommands.CreateBugReportCommand(
                dto.getTitle(),
                dto.getDescription(),
                dto.getPageUrl(),
                dto.getEnvironmentJson(),
                dto.getReporterEmail(),
                dto.getFileIds());
    }

    /**
     * 将查询参数转换为列表查询参数
     */
    public GetBugReportListQuery toListQuery(BugReportListQueryDTO dto) {
        return new GetBugReportListQuery(
                dto.getPage(),
                dto.getSize(),
                dto.getStatus());
    }
}
