package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.BugReportResult;
import com.bluenet.web.application.command.bugreport.BugReportCommands;
import com.bluenet.web.application.service.BugReportAdminAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.entity.BugReportImage;
import com.bluenet.web.domain.repository.BugReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bug 报告管理应用服务实现（管理端）。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BugReportAdminAppServiceImpl implements BugReportAdminAppService {

    private final BugReportRepository bugReportRepository;

    @Override
    public Page<BugReportResult.Brief> getBugReportList(BugReportCommands.GetBugReportListCommand command) {
        int page = command.page() != null ? command.page() : 0;
        int size = command.size() != null ? Math.min(command.size(), 100) : 20;
        Pageable pageable = PageRequest.of(page, size);

        Page<BugReport> reportPage = bugReportRepository.findPage(pageable, command.status());
        return reportPage.map(this::toBriefResult);
    }

    @Override
    public BugReportResult.Detail getBugReportDetail(Long id) {
        BugReport bugReport = bugReportRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("Bug 报告不存在"));
        return toDetailResult(bugReport);
    }

    @Override
    @Transactional
    public BugReportResult.Detail updateStatus(BugReportCommands.UpdateBugReportStatusCommand command) {
        BugReport bugReport = bugReportRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("Bug 报告不存在"));

        bugReport.updateStatus(command.status());
        bugReportRepository.updateStatus(command.id(), command.status());

        log.info("更新 Bug 报告状态: id={}, status={}", command.id(), command.status());
        return toDetailResult(bugReport);
    }

    private BugReportResult.Brief toBriefResult(BugReport bugReport) {
        int imageCount = bugReport.getImages() != null ? bugReport.getImages().size() : 0;
        String description = bugReport.getDescription();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 100) + "...";
        }

        return new BugReportResult.Brief(
                bugReport.getId(),
                bugReport.getTitle(),
                description,
                bugReport.getPageUrl(),
                bugReport.getReporterEmail(),
                bugReport.getStatus(),
                imageCount,
                bugReport.getGithubIssueUrl());
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
