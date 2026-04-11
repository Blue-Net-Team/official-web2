package com.bluenet.web.infrastructure.security.change;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class ChangePasswordStateServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private ChangePasswordStateService stateService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void create_shouldStoreStateAndSetTTL() {
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        String token = stateService.create(1L);

        assertNotNull(token);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> stateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(hashOperations).putAll(startsWith("change_pwd:"), stateCaptor.capture());
        Map<String, String> state = stateCaptor.getValue();
        assertEquals("1", state.get("userId"));
        assertEquals("1", state.get("step"));
        assertEquals("true", state.get("verified"));

        verify(redisTemplate).expire(startsWith("change_pwd:"), eq(15L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getField_shouldReturnValue() {
        when(hashOperations.get(startsWith("change_pwd:"), eq("userId"))).thenReturn("42");

        String value = stateService.getField("some-token", "userId");

        assertEquals("42", value);
    }

    @Test
    void getField_shouldReturnNullWhenNotExists() {
        when(hashOperations.get(startsWith("change_pwd:"), eq("userId"))).thenReturn(null);

        String value = stateService.getField("some-token", "userId");

        assertNull(value);
    }

    @Test
    void getStep_shouldReturnStepNumber() {
        when(hashOperations.get(startsWith("change_pwd:"), eq("step"))).thenReturn("2");

        int step = stateService.getStep("some-token");

        assertEquals(2, step);
    }

    @Test
    void getStep_shouldReturnZeroWhenNotExists() {
        when(hashOperations.get(startsWith("change_pwd:"), eq("step"))).thenReturn(null);

        int step = stateService.getStep("some-token");

        assertEquals(0, step);
    }

    @Test
    void exists_shouldReturnTrueWhenKeyPresent() {
        when(redisTemplate.hasKey(startsWith("change_pwd:"))).thenReturn(Boolean.TRUE);

        assertTrue(stateService.exists("some-token"));
    }

    @Test
    void exists_shouldReturnFalseWhenKeyAbsent() {
        when(redisTemplate.hasKey(startsWith("change_pwd:"))).thenReturn(Boolean.FALSE);

        assertFalse(stateService.exists("some-token"));
    }

    @Test
    void delete_shouldRemoveKey() {
        when(redisTemplate.delete(startsWith("change_pwd:"))).thenReturn(true);

        stateService.delete("some-token");

        verify(redisTemplate).delete(startsWith("change_pwd:"));
    }
}
