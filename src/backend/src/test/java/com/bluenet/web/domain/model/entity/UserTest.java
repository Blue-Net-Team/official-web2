package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    void changePassword_shouldUpdatePassword() {
        User user = User.reconstruct(
                1L,
                null,
                null,
                null,
                "old",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        user.changePassword("encoded-new");

        assertEquals("encoded-new", user.getPassword());
    }

    @Test
    void changePassword_withBlankPassword_shouldThrow() {
        User user = User.reconstruct(
                1L,
                null,
                null,
                null,
                "old",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(IllegalArgumentException.class, () -> user.changePassword(" "));
    }
}
