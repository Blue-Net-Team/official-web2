package com.bluenet.web.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class GitHubIssueAsyncConfig {

    private final GitHubAppProperties gitHubAppProperties;

    @PostConstruct
    public void logGitHubIssueSyncStatus() {
        if (gitHubAppProperties.isEnabled()) {
            log.info(
                    "GitHub Issue 同步功能已启用: owner={}, repo={}",
                    gitHubAppProperties.getOwner(),
                    gitHubAppProperties.getRepo());
        } else {
            log.info("GitHub Issue 同步功能未启用");
        }
    }

    @Bean("githubIssueExecutor")
    public Executor githubIssueExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("github-issue-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
