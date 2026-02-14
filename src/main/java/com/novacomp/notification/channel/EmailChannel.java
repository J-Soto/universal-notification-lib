package com.novacomp.notification.channel;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.core.NotificationChannel;
import com.novacomp.notification.core.NotificationResult;
import com.novacomp.notification.model.ChannelType;
import com.novacomp.notification.model.EmailRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementación stub / basada en consola de un canal de notificación
 * por correo electrónico.
 * <p>
 * En un entorno de producción, esta clase sería reemplazada (o extendida)
 * por una implementación específica del proveedor (ej:
 * {@code SendGridEmailChannel}).
 */
@Slf4j
public final class EmailChannel implements NotificationChannel<EmailRequest> {

    private final NotificationConfig config;

    public EmailChannel(NotificationConfig config) {
        this.config = config;
        log.debug("EmailChannel inicializado con config: from={}",
                config.getProperty("email.from", "no-configurado"));
    }

    @Override
    public NotificationResult send(EmailRequest request) {
        String provider = config.getProperty("email.provider", "sendgrid");
        String from = config.getProperty("email.from", "no-configurado");

        log.info("[EMAIL] Proveedor={} | De='{}' -> Para='{}', Asunto='{}'",
                provider, from, request.to(), request.subject());

        try {
            // -- Simulacion de la respuesta de SendGrid v3 API --
            // POST https://api.sendgrid.com/v3/mail/send
            // Response: HTTP 202 Accepted + X-Message-Id header
            String messageId = "SG." + UUID.randomUUID().toString().replace("-", "");

            log.info("[EMAIL] [SendGrid] HTTP 202 Accepted | X-Message-Id={}", messageId);
            log.debug("[EMAIL] [SendGrid] Response: {{ \"status\": 202, \"message\": \"success\" }}");
            return new NotificationResult.Success(messageId, Instant.now());

        } catch (Exception ex) {
            log.error("[EMAIL] Fallo en el envio | error={}", ex.getMessage(), ex);
            return new NotificationResult.Failure("EMAIL_SEND_ERROR", ex.getMessage());
        }
    }

    @Override
    public ChannelType getType() {
        return ChannelType.EMAIL;
    }
}
