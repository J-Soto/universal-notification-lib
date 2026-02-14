package com.novacomp.notification.core;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.model.EmailRequest;
import com.novacomp.notification.model.PushRequest;
import com.novacomp.notification.model.SmsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationService -- Fachada / prueba de extremo a extremo")
class NotificationServiceTest {

    private NotificationService service;

    @BeforeEach
    void setUp() {
        var config = NotificationConfig.builder()
                .property("email.from", "noreply@novacomp.com")
                .property("sms.provider", "twilio")
                .retryAttempts(2)
                .build();
        service = new NotificationService(config);
    }

    // ------------------------------------------------------------------ //
    // Auto-despacho (pattern matching sobre el tipo de solicitud)
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("auto-despacha EmailRequest al canal EMAIL")
    void autoDispatchEmail() {
        var result = service.send(new EmailRequest("u@e.com", "Hola", "Cuerpo"));
        assertInstanceOf(NotificationResult.Success.class, result);
    }

    @Test
    @DisplayName("auto-despacha SmsRequest al canal SMS")
    void autoDispatchSms() {
        var result = service.send(new SmsRequest("+123", "Hola"));
        assertInstanceOf(NotificationResult.Success.class, result);
    }

    @Test
    @DisplayName("auto-despacha PushRequest al canal PUSH")
    void autoDispatchPush() {
        var result = service.send(new PushRequest("tok", "T", "C"));
        assertInstanceOf(NotificationResult.Success.class, result);
    }

    @Test
    @DisplayName("rechaza tipo de solicitud no soportado")
    void rejectsUnknownRequestType() {
        assertThrows(IllegalArgumentException.class,
                () -> service.send("no-es-una-solicitud"));
    }

    @Test
    @DisplayName("rechaza solicitud nula")
    void rejectsNullRequest() {
        assertThrows(NullPointerException.class,
                () -> service.send((Object) null));
    }

    // ------------------------------------------------------------------ //
    // Pattern matching sobre el resultado
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("el resultado Success contiene un messageId no vacío")
    void successResultHasMessageId() {
        var result = service.send(new EmailRequest("u@e.com", "A", "C"));

        String id = switch (result) {
            case NotificationResult.Success s -> s.messageId();
            case NotificationResult.Failure f -> fail("Se esperaba éxito, se obtuvo: " + f.reason());
        };

        assertNotNull(id);
        assertFalse(id.isBlank());
    }
}
