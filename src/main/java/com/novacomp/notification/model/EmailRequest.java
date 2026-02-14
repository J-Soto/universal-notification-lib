package com.novacomp.notification.model;

import java.util.Objects;

/**
 * Value Object inmutable que representa una solicitud de notificación
 * por correo electrónico.
 * <p>
 * Utiliza un {@code record} de Java 21 con un constructor canónico compacto
 * que garantiza invariantes de no-nulidad en tiempo de construcción.
 *
 * @param to      dirección de correo del destinatario (no puede ser
 *                {@code null} ni vacía)
 * @param subject línea de asunto del correo (no puede ser {@code null})
 * @param body    contenido del cuerpo del correo (no puede ser {@code null})
 */
public record EmailRequest(String to, String subject, String body) {

    public EmailRequest {
        Objects.requireNonNull(to, "El destinatario 'to' no puede ser nulo");
        Objects.requireNonNull(subject, "El asunto no puede ser nulo");
        Objects.requireNonNull(body, "El cuerpo no puede ser nulo");

        if (to.isBlank()) {
            throw new IllegalArgumentException("El destinatario 'to' no puede estar vacío");
        }
    }
}
