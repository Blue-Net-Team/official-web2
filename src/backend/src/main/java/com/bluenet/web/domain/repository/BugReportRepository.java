package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BugReportRepository {

    /**
     * 保存新的 Bug 报告记录。
     *
     * @param bugReport
     *            Bug 报告领域对象
     */
    void save(BugReport bugReport);

    /**
     * 按主键查询 Bug 报告记录。
     *
     * @param id
     *            业务记录主键
     * @return 查询到的 Bug 报告结果；不存在时为空
     */
    Optional<BugReport> findById(Long id);

    /**
     * 分页查询 Bug 报告列表。
     *
     * @param pageable
     *            分页参数
     * @param status
     *            状态筛选
     * @return 分页后的 Bug 报告结果
     */
    Page<BugReport> findPage(Pageable pageable, BugReportStatus status);

    /**
     * 更新 Bug 报告状态。
     *
     * @param id
     *            Bug 报告 ID
     * @param status
     *            新状态
     * @return 数据库受影响行数
     */
    int updateStatus(Long id, BugReportStatus status);
}
