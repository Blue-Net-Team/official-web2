package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.bugreport.BugReportResult;
import com.bluenet.web.application.query.bugreport.GetBugReportListQuery;
import com.bluenet.web.application.service.BugReportAdminAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.entity.BugReportImage;
import com.bluenet.web.domain.repository.BugReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bug 报告管理应用服务实现（管理端）。
 */
@Service
@RequiredArgsConstructor
public class BugReportAdminAppServiceImpl implements BugReportAdminAppService {

    private final BugReportRepository bugReportRepository;

    @Override
    public Page<BugReportResult.Brief> getBugReportList(GetBugReportListQuery query) {
        int page = query.page() != null ? query.page() : 0;
        int size = query.size() != null ? Math.min(query.size(), 100) : 20;
        Pageable pageable = PageRequest.of(page, size);

        Page<BugReport> reportPage = bugReportRepository.findPage(pageable, query.status());
        return reportPage.map(this::toBriefResult);
    }

    @Override
    public BugReportResult.Detail getBugReportDetail(Long id) {
        BugReport bugReport = bugReportRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("Bug 报告不存在"));
        return toDetailResult(bugReport);
    }

    private BugReportResult.Brief toBriefResult(BugReport bugReport) {
        int imageCount = bugReport.getImages() != null ? bugReport.getImages().size() : 0;

        return new BugReportResult.Brief(
                bugReport.getId(),
                bugReport.getTitle(),
                bugReport.getDescription(),
                bugReport.getPageUrl(),
                bugReport.getReporterEmail(),
                bugReport.getStatus(),
                imageCount,
                bugReport.getGithubIssueUrl(),
                bugReport.getGithubIssueNumber());
    }

    private BugReportResult.Detail toDetailResult(BugReport bugReport) {
        List<Long> fileIds = bugReport.getImages() != null
                ? bugReport.getImages().stream().map(BugReportImage::getFileId).toList()
                : List.of();

        return new BugReportResult.Detail(
                bugReport.getId(),
                bugReport.getTitle(),
                bugReport.getDescription(),
                bugReport.getPageUrl(),
                bugReport.getEnvironmentJson(),
                bugReport.getReporterEmail(),
                bugReport.getStatus(),
                bugReport.getGithubIssueUrl(),
                bugReport.getGithubIssueNumber(),
                fileIds);
    }
}
