package com.novacomp.notification.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PushRequest -- Validacion de Record")
class PushRequestTest {

    @Test
    @DisplayName("debe crear un PushRequest válido")
    void shouldCreateValidRequest() {
        var request = new PushRequest("device-token-abc", "Título", "Cuerpo");

        assertEquals("device-token-abc", request.deviceToken());
        assertEquals("Título", request.title());
        assertEquals("Cuerpo", request.body());
    }

    @Test
    @DisplayName("debe rechazar deviceToken nulo")
    void shouldRejectNullToken() {
        assertThrows(NullPointerException.class,
                () -> new PushRequest(null, "T", "C"));
    }

    @Test
    @DisplayName("debe rechazar deviceToken vacío")
    void shouldRejectBlankToken() {
        assertThrows(IllegalArgumentException.class,
                () -> new PushRequest("   ", "T", "C"));
    }

    @Test
    @DisplayName("debe rechazar title nulo")
    void shouldRejectNullTitle() {
        assertThrows(NullPointerException.class,
                () -> new PushRequest("token", null, "C"));
    }

    @Test
    @DisplayName("debe rechazar body nulo")
    void shouldRejectNullBody() {
        assertThrows(NullPointerException.class,
                () -> new PushRequest("token", "T", null));
    }
}
