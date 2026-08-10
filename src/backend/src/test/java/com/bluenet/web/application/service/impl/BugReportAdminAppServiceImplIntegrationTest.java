package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.query.bugreport.GetBugReportListQuery;
import com.bluenet.web.application.result.bugreport.BugReportResult;
import com.bluenet.web.application.service.BugReportAdminAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BugReportAdminAppServiceImpl 集成测试。
 *
 * <p>
 * 验证管理端 Bug 报告分页查询、详情查询及状态过滤逻辑。
 * </p>
 */
@DisplayName("BugReportAdminAppServiceImpl 集成测试")
@WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
class BugReportAdminAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BugReportAdminAppService bugReportAdminAppService;

    @Autowired
    private BugReportRepository bugReportRepository;

    private BugReport createBugReport(String title, String description, BugReportStatus status) {
        BugReport bugReport = BugReport.create(title, description, null, null, null, null);
        bugReport.updateStatus(status);
        bugReportRepository.save(bugReport);
        return bugReport;
    }

    private BugReport createBugReportWithImages(String title, String description, BugReportStatus status,
            List<Long> fileIds) {
        BugReport bugReport = BugReport.create(
                title,
                description,
                "https://example.com/page",
                "{\"browser\":\"Chrome\"}",
                "reporter@example.com",
                fileIds);
        bugReport.updateStatus(status);
        bugReportRepository.save(bugReport);
        return bugReport;
    }

    @Test
    @DisplayName("getBugReportList: 无状态筛选应返回全部报告")
    void getBugReportList_noFilter_shouldReturnAll() {
        BugReport pending = createBugReport("待处理报告", "描述1", BugReportStatus.PENDING);
        BugReport resolved = createBugReport("已解决报告", "描述2", BugReportStatus.RESOLVED);

        Page<BugReportResult.Brief> result = bugReportAdminAppService.getBugReportList(
                new GetBugReportListQuery(0, 10, null));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(BugReportResult.Brief::id)
                .containsExactlyInAnyOrder(pending.getId(), resolved.getId());
    }

    @Test
    @DisplayName("getBugReportList: 按状态筛选应只返回匹配报告")
    void getBugReportList_filterByStatus_shouldReturnMatched() {
        BugReport pending = createBugReport("待处理报告", "描述1", BugReportStatus.PENDING);
        createBugReport("已解决报告", "描述2", BugReportStatus.RESOLVED);

        Page<BugReportResult.Brief> result = bugReportAdminAppService.getBugReportList(
                new GetBugReportListQuery(0, 10, BugReportStatus.PENDING));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(pending.getId());
        assertThat(result.getContent().get(0).status()).isEqualTo(BugReportStatus.PENDING);
    }

    @Test
    @DisplayName("getBugReportList: 空查询应使用默认分页参数")
    void getBugReportList_emptyQuery_shouldUseDefaults() {
        createBugReport("默认分页报告", "描述", BugReportStatus.PENDING);

        Page<BugReportResult.Brief> result = bugReportAdminAppService.getBugReportList(
                new GetBugReportListQuery(null, null, null));

        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(20);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getBugReportList: 每页大小应被限制为 100")
    void getBugReportList_sizeTooLarge_shouldCapAt100() {
        createBugReport("大分页报告", "描述", BugReportStatus.PENDING);

        Page<BugReportResult.Brief> result = bugReportAdminAppService.getBugReportList(
                new GetBugReportListQuery(0, 200, null));

        assertThat(result.getPageable().getPageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("getBugReportList: 返回项应包含图片数量")
    void getBugReportList_shouldIncludeImageCount() {
        BugReport report = createBugReportWithImages(
                "带图报告",
                "描述",
                BugReportStatus.IN_PROGRESS,
                List.of(10L, 20L, 30L));

        Page<BugReportResult.Brief> result = bugReportAdminAppService.getBugReportList(
                new GetBugReportListQuery(0, 10, null));

        assertThat(result.getContent()).hasSize(1);
        BugReportResult.Brief brief = result.getContent().get(0);
        assertThat(brief.id()).isEqualTo(report.getId());
        assertThat(brief.imageCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("getBugReportDetail: 存在时应返回详情及图片 ID 列表")
    void getBugReportDetail_existing_shouldReturnDetail() {
        BugReport bugReport = createBugReportWithImages(
                "详情报告",
                "描述",
                BugReportStatus.IN_PROGRESS,
                List.of(10L, 20L));

        BugReportResult.Detail result = bugReportAdminAppService.getBugReportDetail(bugReport.getId());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(bugReport.getId());
        assertThat(result.title()).isEqualTo("详情报告");
        assertThat(result.description()).isEqualTo("描述");
        assertThat(result.pageUrl()).isEqualTo("https://example.com/page");
        assertThat(result.environmentJson()).isEqualTo("{\"browser\":\"Chrome\"}");
        assertThat(result.reporterEmail()).isEqualTo("reporter@example.com");
        assertThat(result.status()).isEqualTo(BugReportStatus.IN_PROGRESS);
        assertThat(result.fileIds()).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("getBugReportDetail: 不存在时应抛 DataNotFound")
    void getBugReportDetail_notExisting_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> bugReportAdminAppService.getBugReportDetail(-1L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("Bug 报告不存在");
    }
}
