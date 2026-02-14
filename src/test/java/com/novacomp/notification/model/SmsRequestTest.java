package com.novacomp.notification.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SmsRequest -- Validacion de Record")
class SmsRequestTest {

    @Test
    @DisplayName("debe crear un SmsRequest válido")
    void shouldCreateValidRequest() {
        var request = new SmsRequest("+1234567890", "Hola vía SMS");

        assertEquals("+1234567890", request.phoneNumber());
        assertEquals("Hola vía SMS", request.message());
    }

    @Test
    @DisplayName("debe rechazar phoneNumber nulo")
    void shouldRejectNullPhone() {
        assertThrows(NullPointerException.class,
                () -> new SmsRequest(null, "msg"));
    }

    @Test
    @DisplayName("debe rechazar phoneNumber vacío")
    void shouldRejectBlankPhone() {
        assertThrows(IllegalArgumentException.class,
                () -> new SmsRequest("  ", "msg"));
    }

    @Test
    @DisplayName("debe rechazar message nulo")
    void shouldRejectNullMessage() {
        assertThrows(NullPointerException.class,
                () -> new SmsRequest("+1234567890", null));
    }
}
