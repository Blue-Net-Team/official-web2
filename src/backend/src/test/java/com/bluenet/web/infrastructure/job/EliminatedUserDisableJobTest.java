package com.bluenet.web.infrastructure.job;

import com.bluenet.web.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * EliminatedUserDisableJob 单元测试。
 */
@DisplayName("EliminatedUserDisableJob 单元测试")
@ExtendWith(MockitoExtension.class)
class EliminatedUserDisableJobTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EliminatedUserDisableJob eliminatedUserDisableJob;

    @Test
    @DisplayName("有淘汰超过7天的考生：应禁用账号")
    void disableEliminatedUsers_hasExpiredEliminations_shouldDisableUsers() {
        List<Long> userIds = List.of(100L, 101L);
        when(userRepository.findUserIdsToDisableByElimination(any(LocalDateTime.class)))
                .thenReturn(userIds);

        eliminatedUserDisableJob.disableEliminatedUsers();

        verify(userRepository).batchUpdateDisable(List.of(100L), true);
        verify(userRepository).batchUpdateDisable(List.of(101L), true);
    }

    @Test
    @DisplayName("无淘汰超过7天的考生：不应执行禁用")
    void disableEliminatedUsers_noExpiredEliminations_shouldDoNothing() {
        when(userRepository.findUserIdsToDisableByElimination(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        eliminatedUserDisableJob.disableEliminatedUsers();

        verify(userRepository, never()).batchUpdateDisable(any(), any());
    }

    @Test
    @DisplayName("查询失败：应终止任务不抛异常")
    void disableEliminatedUsers_queryFails_shouldNotThrow() {
        when(userRepository.findUserIdsToDisableByElimination(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        assertDoesNotThrow(() -> eliminatedUserDisableJob.disableEliminatedUsers());
        verify(userRepository, never()).batchUpdateDisable(any(), any());
    }

    @Test
    @DisplayName("部分禁用失败：应继续处理剩余用户")
    void disableEliminatedUsers_partialFailure_shouldContinue() {
        List<Long> userIds = List.of(100L, 101L);
        when(userRepository.findUserIdsToDisableByElimination(any(LocalDateTime.class)))
                .thenReturn(userIds);
        doThrow(new RuntimeException("锁定冲突"))
                .when(userRepository)
                .batchUpdateDisable(List.of(100L), true);

        assertDoesNotThrow(() -> eliminatedUserDisableJob.disableEliminatedUsers());
        verify(userRepository).batchUpdateDisable(List.of(101L), true);
    }
}
