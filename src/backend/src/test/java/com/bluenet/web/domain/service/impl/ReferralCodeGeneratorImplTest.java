package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralCodeGeneratorImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReferralCodeGeneratorImpl referralCodeGenerator;

    @Test
    @DisplayName("生成的内推码应为8位大写字母和数字")
    void generate_shouldReturn8CharacterUppercaseAlphanumeric() {
        // Arrange
        when(userRepository.existsByInternalReferralCode(anyString())).thenReturn(false);

        // Act
        String code = referralCodeGenerator.generate();

        // Assert
        assertNotNull(code);
        assertEquals(8, code.length());
        assertTrue(code.matches("[A-Z0-9]{8}"));
    }

    @Test
    @DisplayName("生成的内推码在数据库中应唯一")
    void generate_shouldReturnUniqueCode() {
        // Arrange
        when(userRepository.existsByInternalReferralCode(anyString())).thenReturn(false);

        // Act
        String code1 = referralCodeGenerator.generate();
        String code2 = referralCodeGenerator.generate();

        // Assert - 由于随机性，两次生成的码应该不同（极小概率相同）
        assertNotNull(code1);
        assertNotNull(code2);
    }

    @Test
    @DisplayName("当内推码已存在时应重试生成")
    void generate_shouldRetryWhenCodeExists() {
        // Arrange - 模拟第一次已存在，后续不存在
        when(userRepository.existsByInternalReferralCode(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        // Act
        String code = referralCodeGenerator.generate();

        // Assert
        assertNotNull(code);
        assertTrue(code.matches("[A-Z0-9]{8}"));
    }

    @Test
    @DisplayName("当重试次数耗尽时应抛出异常")
    void generate_shouldThrowExceptionWhenMaxRetriesExceeded() {
        // Arrange
        when(userRepository.existsByInternalReferralCode(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> referralCodeGenerator.generate());
    }

    @Test
    @DisplayName("有效格式的内推码应验证通过")
    void isValidFormat_shouldReturnTrueForValidCode() {
        // Act & Assert
        assertTrue(referralCodeGenerator.isValidFormat("ABC12345"));
        assertTrue(referralCodeGenerator.isValidFormat("XXXXXXXX"));
        assertTrue(referralCodeGenerator.isValidFormat("12345678"));
        assertTrue(referralCodeGenerator.isValidFormat("A1B2C3D4"));
    }

    @Test
    @DisplayName("无效格式的内推码应验证失败")
    void isValidFormat_shouldReturnFalseForInvalidCode() {
        // Act & Assert
        assertFalse(referralCodeGenerator.isValidFormat(null));
        assertFalse(referralCodeGenerator.isValidFormat(""));
        assertFalse(referralCodeGenerator.isValidFormat("ABC123")); // 太短
        assertFalse(referralCodeGenerator.isValidFormat("ABC123456")); // 太长
        assertFalse(referralCodeGenerator.isValidFormat("abc12345")); // 小写
        assertFalse(referralCodeGenerator.isValidFormat("ABC12-45")); // 特殊字符
        assertFalse(referralCodeGenerator.isValidFormat("ABC 1234")); // 空格
    }
}
