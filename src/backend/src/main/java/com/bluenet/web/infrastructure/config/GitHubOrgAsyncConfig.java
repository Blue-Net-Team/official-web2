package com.bluenet.web.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * GitHub 组织邀请异步配置。
 * <p>
 * 提供独立线程池用于异步发送组织邀请，避免阻塞考核发布等主流程。
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GitHubOrgAsyncConfig {

    private final GitHubAppsProperties appsProperties;

    @PostConstruct
    public void logGitHubOrgInvitationStatus() {
        GitHubAppConfig config = appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME);
        if (config != null && config.isEnabled()) {
            log.info("GitHub 组织邀请功能已启用: org={}", config.getOrg());
        } else {
            log.info("GitHub 组织邀请功能未启用");
        }
    }

    @Bean("githubOrgExecutor")
    public Executor githubOrgExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("github-org-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
