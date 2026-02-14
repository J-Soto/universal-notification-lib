package com.novacomp.notification.model;

import java.util.Objects;

/**
 * Value Object inmutable que representa una solicitud de notificación push.
 *
 * @param deviceToken token del dispositivo destino (no puede ser {@code null}
 *                    ni vacío)
 * @param title       título de la notificación (no puede ser {@code null})
 * @param body        contenido del cuerpo de la notificación (no puede ser
 *                    {@code null})
 */
public record PushRequest(String deviceToken, String title, String body) {

    public PushRequest {
        Objects.requireNonNull(deviceToken, "El token del dispositivo no puede ser nulo");
        Objects.requireNonNull(title, "El título no puede ser nulo");
        Objects.requireNonNull(body, "El cuerpo no puede ser nulo");

        if (deviceToken.isBlank()) {
            throw new IllegalArgumentException("El token del dispositivo no puede estar vacío");
        }
    }
}
