package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    void changePassword_shouldUpdatePassword() {
        User user = User.builder().id(1L).password("old").build();

        user.changePassword("encoded-new");

        assertEquals("encoded-new", user.getPassword());
    }

    @Test
    void changePassword_withBlankPassword_shouldThrow() {
        User user = User.builder().id(1L).password("old").build();

        assertThrows(IllegalArgumentException.class, () -> user.changePassword(" "));
    }
}
