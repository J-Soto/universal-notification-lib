package com.novacomp.notification.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationResult -- Interfaz sellada")
class NotificationResultTest {

    @Test
    @DisplayName("el record Success almacena messageId y timestamp")
    void successHoldsData() {
        var now = Instant.now();
        var success = new NotificationResult.Success("msg-123", now);

        assertEquals("msg-123", success.messageId());
        assertEquals(now, success.timestamp());
        assertInstanceOf(NotificationResult.class, success);
    }

    @Test
    @DisplayName("el record Failure almacena code y reason")
    void failureHoldsData() {
        var failure = new NotificationResult.Failure("ERR_01", "timeout");

        assertEquals("ERR_01", failure.code());
        assertEquals("timeout", failure.reason());
        assertInstanceOf(NotificationResult.class, failure);
    }

    @Test
    @DisplayName("Success rechaza messageId nulo")
    void successRejectsNullId() {
        assertThrows(NullPointerException.class,
                () -> new NotificationResult.Success(null, Instant.now()));
    }

    @Test
    @DisplayName("Failure rechaza code nulo")
    void failureRejectsNullCode() {
        assertThrows(NullPointerException.class,
                () -> new NotificationResult.Failure(null, "razón"));
    }

    @Test
    @DisplayName("el switch con pattern matching es exhaustivo sobre los permits del sealed")
    void patternMatchingSwitchIsExhaustive() {
        NotificationResult result = new NotificationResult.Success("id", Instant.now());

        // El compilador garantiza que este switch es exhaustivo porque
        // NotificationResult es sealed con exactamente dos permits.
        String output = switch (result) {
            case NotificationResult.Success s -> "OK:" + s.messageId();
            case NotificationResult.Failure f -> "ERR:" + f.code();
        };

        assertTrue(output.startsWith("OK:"));
    }
}
