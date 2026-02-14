package com.novacomp.notification.channel;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.core.NotificationChannel;
import com.novacomp.notification.core.NotificationResult;
import com.novacomp.notification.model.ChannelType;
import com.novacomp.notification.model.PushRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementación stub / basada en consola de un canal de notificación push.
 * <p>
 * Sería reemplazada por un adaptador de proveedor como FCM, APNs o similar.
 */
@Slf4j
public final class PushChannel implements NotificationChannel<PushRequest> {

    private final NotificationConfig config;

    public PushChannel(NotificationConfig config) {
        this.config = config;
        log.debug("PushChannel inicializado");
    }

    @Override
    public NotificationResult send(PushRequest request) {
        String provider = config.getProperty("push.provider", "fcm");
        String projectId = config.getProperty("push.project.id", "novacomp-demo");

        log.info("[PUSH] Proveedor={} | Dispositivo='{}', Titulo='{}'",
                provider, request.deviceToken(), request.title());

        try {
            // -- Simulacion de la respuesta de Firebase Cloud Messaging v1 API --
            // POST https://fcm.googleapis.com/v1/projects/{project}/messages:send
            // Response: name = "projects/{project}/messages/{id}"
            String fcmId = UUID.randomUUID().toString().substring(0, 19);
            String messageName = "projects/" + projectId + "/messages/" + fcmId;

            log.info("[PUSH] [FCM] name={} | priority=high | ttl=2419200s", messageName);
            log.debug("[PUSH] [FCM] Response: {{ \"name\": \"{}\" }}", messageName);
            return new NotificationResult.Success(messageName, Instant.now());

        } catch (Exception ex) {
            log.error("[PUSH] Fallo en el envio | error={}", ex.getMessage(), ex);
            return new NotificationResult.Failure("PUSH_SEND_ERROR", ex.getMessage());
        }
    }

    @Override
    public ChannelType getType() {
        return ChannelType.PUSH;
    }
}
