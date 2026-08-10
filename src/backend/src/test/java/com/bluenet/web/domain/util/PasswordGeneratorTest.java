package com.bluenet.web.domain.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    @Test
    @DisplayName("Should generate password with requested length")
    void generate_length() {
        String password = PasswordGenerator.generate(10, true);

        assertEquals(10, password.length());
    }

    @Test
    @DisplayName("Should include special characters when requested")
    void generate_includeSpecial() {
        String password = PasswordGenerator.generate(20, true);

        assertTrue(password.chars().anyMatch(c -> "!@#%^&*".indexOf(c) >= 0));
    }

    @Test
    @DisplayName("Should not include special characters when disabled")
    void generate_noSpecial() {
        String password = PasswordGenerator.generate(20, false);

        assertTrue(password.chars().allMatch(Character::isLetterOrDigit));
    }

    @RepeatedTest(20)
    @DisplayName("Should not generate password with regex replacement special characters")
    void generate_noRegexSpecialChars() {
        String password = PasswordGenerator.generate(10, true);

        assertFalse(password.contains("$"), "Password should not contain $: " + password);
        assertFalse(password.contains("\\"), "Password should not contain backslash: " + password);
    }
}
