package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.BugReportResult;
import com.bluenet.web.application.command.bugreport.BugReportCommands;
import com.bluenet.web.application.service.BugReportAppService;
import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.repository.BugReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bug 报告应用服务实现（公开端）。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BugReportAppServiceImpl implements BugReportAppService {

    private final BugReportRepository bugReportRepository;
    private final GitHubIssueSyncService gitHubIssueSyncService;

    @Override
    @Transactional
    public BugReportResult.Created submitBugReport(BugReportCommands.CreateBugReportCommand command) {
        BugReport bugReport = BugReport.create(
                command.description(),
                command.pageUrl(),
                command.environmentJson(),
                command.reporterEmail(),
                command.fileIds());

        bugReportRepository.save(bugReport);
        log.info("提交 Bug 报告: id={}", bugReport.getId());

        // 异步同步到 GitHub Issue（失败不影响用户提交体验）
        gitHubIssueSyncService.sync(bugReport);

        return new BugReportResult.Created(bugReport.getId(), bugReport.getStatus(), null);
    }
}
