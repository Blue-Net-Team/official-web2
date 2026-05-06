package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.bugreport.BugReportBriefDTO;
import com.bluenet.web.api.dto.bugreport.BugReportDetailDTO;
import com.bluenet.web.api.dto.bugreport.BugReportCreatedDTO;
import com.bluenet.web.application.BugReportResult;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bug 报告应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class BugReportAppConverter {

    public BugReportCreatedDTO toCreatedDTO(BugReportResult.Created result) {
        return BugReportCreatedDTO.builder()
                .id(result.id())
                .status(result.status())
                .githubIssueUrl(result.githubIssueUrl())
                .build();
    }

    public BugReportDetailDTO toDetailDTO(BugReportResult.Detail result) {
        return BugReportDetailDTO.builder()
                .id(result.id())
                .description(result.description())
                .pageUrl(result.pageUrl())
                .environmentJson(result.environmentJson())
                .reporterEmail(result.reporterEmail())
                .status(result.status())
                .githubIssueUrl(result.githubIssueUrl())
                .githubIssueNumber(result.githubIssueNumber())
                .fileIds(result.fileIds())
                .build();
    }

    public BugReportBriefDTO toBriefDTO(BugReportResult.Brief result) {
        return BugReportBriefDTO.builder()
                .id(result.id())
                .description(result.description())
                .pageUrl(result.pageUrl())
                .reporterEmail(result.reporterEmail())
                .status(result.status())
                .imageCount(result.imageCount())
                .githubIssueUrl(result.githubIssueUrl())
                .build();
    }

    public List<BugReportBriefDTO> toBriefDTOList(List<BugReportResult.Brief> results) {
        return results.stream()
                .map(this::toBriefDTO)
                .toList();
    }

    public Page<BugReportBriefDTO> toBriefDTOPage(Page<BugReportResult.Brief> page) {
        return page.map(this::toBriefDTO);
    }
}
