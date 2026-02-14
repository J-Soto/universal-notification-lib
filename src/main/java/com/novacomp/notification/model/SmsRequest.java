package com.novacomp.notification.model;

import java.util.Objects;

/**
 * Value Object inmutable que representa una solicitud de notificación SMS.
 *
 * @param phoneNumber número de teléfono destino (no puede ser {@code null} ni
 *                    vacío)
 * @param message     contenido del mensaje SMS (no puede ser {@code null})
 */
public record SmsRequest(String phoneNumber, String message) {

    public SmsRequest {
        Objects.requireNonNull(phoneNumber, "El número de teléfono no puede ser nulo");
        Objects.requireNonNull(message, "El mensaje no puede ser nulo");

        if (phoneNumber.isBlank()) {
            throw new IllegalArgumentException("El número de teléfono no puede estar vacío");
        }
    }
}
