package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.dataobject.BugReportDO;
import com.bluenet.web.infrastructure.repository.mapper.BugReportImageMapper;
import com.bluenet.web.infrastructure.repository.mapper.BugReportMapper;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BugReportRepositoryImpl 集成测试。
 */
@DisplayName("BugReportRepositoryImpl 集成测试")
class BugReportRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BugReportRepository bugReportRepository;

    @Autowired
    private BugReportMapper bugReportMapper;

    @Autowired
    private BugReportImageMapper bugReportImageMapper;

    @Autowired
    private FileRepository fileRepository;

    private final AtomicLong counter = new AtomicLong(1);

    private File createFile() {
        String name = "bug-report-" + counter.getAndIncrement() + ".png";
        return FileFixture.save(fileRepository, name, FileType.NORMAL_IMG);
    }

    private BugReport createBugReport(String title, BugReportStatus status) {
        File file = createFile();
        BugReport bugReport = BugReport.create(
                title,
                title + "描述",
                "http://example.com/page",
                "{}",
                "reporter@example.com",
                List.of(file.getId()));
        if (status == BugReportStatus.IN_PROGRESS) {
            bugReport.updateStatus(BugReportStatus.IN_PROGRESS);
        } else if (status == BugReportStatus.RESOLVED) {
            bugReport.updateStatus(BugReportStatus.RESOLVED);
        }
        bugReportRepository.save(bugReport);
        return bugReport;
    }

    @Test
    @DisplayName("save: 新Bug报告应插入并回写ID")
    void save_newBugReport_shouldInsertAndReturnId() {
        BugReport bugReport = createBugReport("新Bug", BugReportStatus.PENDING);

        assertThat(bugReport.getId()).isNotNull();
        BugReportDO dataObject = bugReportMapper.selectById(bugReport.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getTitle()).isEqualTo("新Bug");
        assertThat(bugReportImageMapper.selectByBugReportId(bugReport.getId())).hasSize(1);
    }

    @Test
    @DisplayName("save: 已有Bug报告应更新字段")
    void save_existingBugReport_shouldUpdateFields() {
        BugReport bugReport = createBugReport("待更新Bug", BugReportStatus.PENDING);
        bugReport.updateStatus(BugReportStatus.IN_PROGRESS);

        bugReportRepository.save(bugReport);

        BugReportDO updated = bugReportMapper.selectById(bugReport.getId());
        assertThat(updated.getStatus()).isEqualTo(BugReportStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        BugReport bugReport = createBugReport("查询Bug", BugReportStatus.PENDING);

        Optional<BugReport> found = bugReportRepository.findById(bugReport.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("查询Bug");
        assertThat(found.get().getImages()).hasSize(1);

        assertThat(bugReportRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findPage: 应按状态分页查询")
    void findPage_shouldFilterByStatus() {
        BugReport pendingReport = createBugReport("待处理Bug", BugReportStatus.PENDING);
        createBugReport("已解决Bug", BugReportStatus.RESOLVED);

        Page<BugReport> page = bugReportRepository.findPage(PageRequest.of(0, 10), BugReportStatus.PENDING);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(pendingReport.getId());
    }

    @Test
    @DisplayName("findByGithubIssueNumber: 应按GitHub Issue编号查询")
    void findByGithubIssueNumber_shouldReturnBugReport() {
        BugReport bugReport = createBugReport("同步GitHub的Bug", BugReportStatus.PENDING);
        bugReport.updateGithubIssueInfo("http://github.com/issue/1", 1);
        bugReportRepository.save(bugReport);

        Optional<BugReport> found = bugReportRepository.findByGithubIssueNumber(1);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(bugReport.getId());

        assertThat(bugReportRepository.findByGithubIssueNumber(999)).isEmpty();
    }
}
