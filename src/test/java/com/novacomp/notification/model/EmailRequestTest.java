package com.novacomp.notification.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailRequest -- Validacion de Record")
class EmailRequestTest {

    @Test
    @DisplayName("debe crear un EmailRequest válido")
    void shouldCreateValidRequest() {
        var request = new EmailRequest("user@example.com", "Hola", "Cuerpo");

        assertEquals("user@example.com", request.to());
        assertEquals("Hola", request.subject());
        assertEquals("Cuerpo", request.body());
    }

    @Test
    @DisplayName("debe rechazar 'to' nulo")
    void shouldRejectNullTo() {
        assertThrows(NullPointerException.class,
                () -> new EmailRequest(null, "Asunto", "Cuerpo"));
    }

    @Test
    @DisplayName("debe rechazar 'to' vacío")
    void shouldRejectBlankTo() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailRequest("   ", "Asunto", "Cuerpo"));
    }

    @Test
    @DisplayName("debe rechazar subject nulo")
    void shouldRejectNullSubject() {
        assertThrows(NullPointerException.class,
                () -> new EmailRequest("a@b.com", null, "Cuerpo"));
    }

    @Test
    @DisplayName("debe rechazar body nulo")
    void shouldRejectNullBody() {
        assertThrows(NullPointerException.class,
                () -> new EmailRequest("a@b.com", "Asunto", null));
    }

    @Test
    @DisplayName("records con los mismos datos deben ser iguales")
    void equalityCheck() {
        var a = new EmailRequest("a@b.com", "A", "C");
        var b = new EmailRequest("a@b.com", "A", "C");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
