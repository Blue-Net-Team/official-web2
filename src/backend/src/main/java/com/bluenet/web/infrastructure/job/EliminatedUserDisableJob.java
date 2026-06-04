package com.bluenet.web.infrastructure.job;

import com.bluenet.web.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 被淘汰考生账号自动禁用定时任务。
 * <p>
 * 每天检查一次，将淘汰超过 7 天的考生账号自动禁用。
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EliminatedUserDisableJob {

    private static final int ELIMINATION_GRACE_DAYS = 7;

    private final UserRepository userRepository;

    @Scheduled(cron = "${job.eliminated-user-disable.cron:0 0 3 * * *}")
    public void disableEliminatedUsers() {
        log.info("开始执行被淘汰考生账号自动禁用任务");

        LocalDateTime cutoffTime = LocalDateTime.now().minus(ELIMINATION_GRACE_DAYS, ChronoUnit.DAYS);
        List<Long> userIds;
        try {
            userIds = userRepository.findUserIdsToDisableByElimination(cutoffTime);
        } catch (Exception e) {
            log.error("查询需要禁用的淘汰考生失败，任务终止", e);
            return;
        }

        if (userIds.isEmpty()) {
            log.info("未发现需要禁用的淘汰考生，任务结束");
            return;
        }

        log.info("发现 {} 个淘汰超过 {} 天的考生账号，开始禁用", userIds.size(), ELIMINATION_GRACE_DAYS);

        int successCount = 0;
        int failureCount = 0;

        for (Long userId : userIds) {
            try {
                userRepository.batchUpdateDisable(List.of(userId), true);
                successCount++;
                log.info("已禁用淘汰考生账号: userId={}", userId);
            } catch (Exception e) {
                failureCount++;
                log.error("禁用淘汰考生账号失败，跳过: userId={}", userId, e);
            }
        }

        log.info("被淘汰考生账号自动禁用任务完成，成功: {}, 失败: {}", successCount, failureCount);
    }
}
