package com.novacomp.notification.core;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.factory.ChannelFactory;
import com.novacomp.notification.model.ChannelType;
import com.novacomp.notification.model.EmailRequest;
import com.novacomp.notification.model.PushRequest;
import com.novacomp.notification.model.SmsRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Fachada / punto de entrada para enviar notificaciones a través de la
 * librería.
 * <p>
 * Coordina el {@link ChannelFactory} y las estrategias tipadas de
 * {@link NotificationChannel} para despachar solicitudes. Utiliza
 * <strong>expresiones switch con pattern matching</strong> sobre el objeto
 * de solicitud para resolver el {@link ChannelType} correcto y realizar
 * el cast seguro al canal tipado.
 *
 * <h3>Ejemplo de uso</h3>
 *
 * <pre>{@code
 * NotificationConfig config = NotificationConfig.builder()
 *         .property("email.from", "noreply@novacomp.com")
 *         .retryAttempts(2)
 *         .build();
 *
 * NotificationService service = new NotificationService(config);
 *
 * NotificationResult result = service.send(
 *         new EmailRequest("user@example.com", "¡Bienvenido!", "Hola."));
 *
 * String output = switch (result) {
 *     case NotificationResult.Success s -> "Enviado: " + s.messageId();
 *     case NotificationResult.Failure f -> "Error: " + f.reason();
 * };
 * }</pre>
 */
@Slf4j
public final class NotificationService {

    private final NotificationConfig config;

    /**
     * Crea una nueva instancia del servicio respaldada por la configuración
     * proporcionada.
     *
     * @param config configuración de la librería (no puede ser {@code null})
     */
    public NotificationService(NotificationConfig config) {
        this.config = Objects.requireNonNull(config, "La NotificationConfig no puede ser nula");
        log.info("Servicio de notificaciones inicializado");
    }

    /**
     * Envía una notificación, resolviendo automáticamente el canal correcto
     * a partir del tipo de solicitud mediante pattern matching.
     *
     * @param request uno de {@link EmailRequest}, {@link SmsRequest},
     *                o {@link PushRequest}
     * @return un {@link NotificationResult}
     * @throws IllegalArgumentException si el tipo de solicitud es desconocido
     */
    public NotificationResult send(Object request) {
        Objects.requireNonNull(request, "La solicitud de notificación no puede ser nula");

        return switch (request) {
            case EmailRequest email -> dispatchEmail(email);
            case SmsRequest sms -> dispatchSms(sms);
            case PushRequest push -> dispatchPush(push);
            default -> throw new IllegalArgumentException(
                    "Tipo de solicitud no soportado: " + request.getClass().getName());
        };
    }

    /**
     * Envía una notificación a través de un tipo de canal específico.
     * <p>
     * El llamador es responsable de asegurar que el tipo de {@code request}
     * coincida con el tipo esperado para el {@link ChannelType} dado.
     *
     * @param type    el canal a utilizar
     * @param request la carga útil de la solicitud de notificación
     * @return un {@link NotificationResult}
     */
    @SuppressWarnings("unchecked")
    public NotificationResult send(ChannelType type, Object request) {
        Objects.requireNonNull(type, "El ChannelType no puede ser nulo");
        Objects.requireNonNull(request, "La solicitud de notificación no puede ser nula");

        var channel = (NotificationChannel<Object>) ChannelFactory.create(type, config);

        log.info("Despachando por canal explícito [tipo={}]", type);
        NotificationResult result = channel.send(request);

        logResult(type, result);
        return result;
    }

    // ------------------------------------------------------------------ //
    // Helpers privados de despacho
    // ------------------------------------------------------------------ //

    private NotificationResult dispatchEmail(EmailRequest request) {
        @SuppressWarnings("unchecked")
        var channel = (NotificationChannel<EmailRequest>) ChannelFactory.create(ChannelType.EMAIL, config);
        log.info("Canal auto-resuelto: EMAIL");
        NotificationResult result = channel.send(request);
        logResult(ChannelType.EMAIL, result);
        return result;
    }

    private NotificationResult dispatchSms(SmsRequest request) {
        @SuppressWarnings("unchecked")
        var channel = (NotificationChannel<SmsRequest>) ChannelFactory.create(ChannelType.SMS, config);
        log.info("Canal auto-resuelto: SMS");
        NotificationResult result = channel.send(request);
        logResult(ChannelType.SMS, result);
        return result;
    }

    private NotificationResult dispatchPush(PushRequest request) {
        @SuppressWarnings("unchecked")
        var channel = (NotificationChannel<PushRequest>) ChannelFactory.create(ChannelType.PUSH, config);
        log.info("Canal auto-resuelto: PUSH");
        NotificationResult result = channel.send(request);
        logResult(ChannelType.PUSH, result);
        return result;
    }

    private void logResult(ChannelType type, NotificationResult result) {
        switch (result) {
            case NotificationResult.Success s ->
                log.info("[OK] [{}] Mensaje entregado [id={}, en={}]",
                        type, s.messageId(), s.timestamp());
            case NotificationResult.Failure f ->
                log.warn("[FAIL] [{}] Mensaje fallido [codigo={}, razon={}]",
                        type, f.code(), f.reason());
        }
    }
}
