package com.novacomp.notification.core;

import java.time.Instant;
import java.util.Objects;

/**
 * Interfaz sellada que representa el resultado de una operación de envío
 * de notificación.
 * <p>
 * Solo existen dos implementaciones permitidas: {@link Success} y
 * {@link Failure}.
 * Esto garantiza exhaustividad en expresiones {@code switch} y
 * {@code instanceof} con pattern matching -- el compilador senalara
 * cualquier caso no manejado en tiempo de compilación (Java 21 sealed
 * + pattern matching).
 *
 * <pre>{@code
 * NotificationResult result = channel.send(request);
 * String output = switch (result) {
 *     case NotificationResult.Success s -> "Enviado: " + s.messageId();
 *     case NotificationResult.Failure f -> "Falló: " + f.reason();
 * };
 * }</pre>
 */
public sealed interface NotificationResult
        permits NotificationResult.Success, NotificationResult.Failure {

    /**
     * Representa una entrega de notificación exitosa.
     *
     * @param messageId identificador único asignado al mensaje enviado
     * @param timestamp instante en el que la notificación fue enviada
     */
    record Success(String messageId, Instant timestamp) implements NotificationResult {

        public Success {
            Objects.requireNonNull(messageId, "El ID del mensaje no puede ser nulo");
            Objects.requireNonNull(timestamp, "El timestamp no puede ser nulo");
        }
    }

    /**
     * Representa una entrega de notificación fallida.
     *
     * @param code   código de error legible por máquina (ej: "INVALID_RECIPIENT")
     * @param reason explicación legible del fallo
     */
    record Failure(String code, String reason) implements NotificationResult {

        public Failure {
            Objects.requireNonNull(code, "El código de error no puede ser nulo");
            Objects.requireNonNull(reason, "La razón del fallo no puede ser nula");
        }
    }
}
