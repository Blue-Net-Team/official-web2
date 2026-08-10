package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.entity.BugReportImage;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.bluenet.web.infrastructure.repository.converter.BugReportRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.BugReportDO;
import com.bluenet.web.infrastructure.repository.mapper.BugReportImageMapper;
import com.bluenet.web.infrastructure.repository.mapper.BugReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Bug 报告 Repository 实现
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class BugReportRepositoryImpl implements BugReportRepository {

    private final BugReportMapper bugReportMapper;
    private final BugReportImageMapper bugReportImageMapper;
    private final BugReportRepositoryConverter converter;

    @Override
    @Transactional
    public void save(BugReport bugReport) {
        BugReportDO dataObject = converter.toDataObject(bugReport);
        if (bugReport.getId() == null) {
            bugReportMapper.insert(dataObject);
            bugReport.setId(dataObject.getId());

            // 保存关联图片
            if (bugReport.getImages() != null) {
                for (BugReportImage image : bugReport.getImages()) {
                    image.setBugReportId(dataObject.getId());
                    bugReportImageMapper.insert(converter.toImageDataObject(image));
                }
            }
        } else {
            bugReportMapper.updateById(dataObject);
        }
        log.info("保存 Bug 报告: id={}", bugReport.getId());
    }

    @Override
    public Optional<BugReport> findById(Long id) {
        BugReportDO dataObject = bugReportMapper.selectById(id);
        if (dataObject == null) {
            return Optional.empty();
        }
        List<BugReportImage> images = converter.toImageEntityList(
                bugReportImageMapper.selectByBugReportId(id));
        return Optional.ofNullable(converter.toEntity(dataObject, images));
    }

    @Override
    public Page<BugReport> findPage(Pageable pageable, BugReportStatus status) {
        QueryWrapper<BugReportDO> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BugReportDO> mpPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber() + 1, pageable.getPageSize());
        mpPage = bugReportMapper.selectPage(mpPage, wrapper);

        List<BugReport> reports = mpPage.getRecords()
                .stream()
                .map(doItem -> {
                    List<BugReportImage> images = converter.toImageEntityList(
                            bugReportImageMapper.selectByBugReportId(doItem.getId()));
                    return converter.toEntity(doItem, images);
                })
                .toList();

        return new PageImpl<>(reports, pageable, mpPage.getTotal());
    }

    @Override
    public Optional<BugReport> findByGithubIssueNumber(Integer githubIssueNumber) {
        QueryWrapper<BugReportDO> wrapper = new QueryWrapper<>();
        wrapper.eq("github_issue_number", githubIssueNumber);
        BugReportDO dataObject = bugReportMapper.selectOne(wrapper);
        if (dataObject == null) {
            return Optional.empty();
        }
        List<BugReportImage> images = converter.toImageEntityList(
                bugReportImageMapper.selectByBugReportId(dataObject.getId()));
        return Optional.ofNullable(converter.toEntity(dataObject, images));
    }
}
