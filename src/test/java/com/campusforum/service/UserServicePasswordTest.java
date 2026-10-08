package com.campusforum.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServicePasswordTest {

    @Test
    void hashesAndVerifiesPasswordsWithBcrypt() {
        String password = "CampusForumTestPassword";
        String firstHash = UserService.hashPassword(password);
        String secondHash = UserService.hashPassword(password);

        assertTrue(firstHash.startsWith("$2a$10$"));
        assertNotEquals(firstHash, secondHash);
        assertTrue(UserService.verifyPassword(password, firstHash));
        assertFalse(UserService.verifyPassword("incorrect-password", firstHash));
    }

    @Test
    void verifiesLegacySha256Passwords() {
        String password = "LegacyPassword";
        String legacyHash = UserService.legacySha256(password);

        assertTrue(legacyHash.matches("[0-9a-f]{64}"));
        assertTrue(UserService.verifyPassword(password, legacyHash));
        assertFalse(UserService.verifyPassword("incorrect-password", legacyHash));
    }

    @Test
    void rejectsNullPasswordInputs() {
        assertFalse(UserService.verifyPassword(null, UserService.hashPassword("password")));
        assertFalse(UserService.verifyPassword("password", null));
    }
}
