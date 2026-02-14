package com.novacomp.notification.channel;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.core.NotificationChannel;
import com.novacomp.notification.core.NotificationResult;
import com.novacomp.notification.model.ChannelType;
import com.novacomp.notification.model.SmsRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementación stub / basada en consola de un canal de notificación SMS.
 * <p>
 * Sería reemplazada por un adaptador de proveedor como Twilio, Vonage o
 * similar.
 */
@Slf4j
public final class SmsChannel implements NotificationChannel<SmsRequest> {

    private final NotificationConfig config;

    public SmsChannel(NotificationConfig config) {
        this.config = config;
        log.debug("SmsChannel inicializado con config: proveedor={}",
                config.getProperty("sms.provider", "no-configurado"));
    }

    @Override
    public NotificationResult send(SmsRequest request) {
        String provider = config.getProperty("sms.provider", "twilio");
        String accountSid = config.getProperty("sms.account.sid", "AC_demo");

        log.info("[SMS] Proveedor={} | Para='{}', Cuenta='{}'",
                provider, request.phoneNumber(), accountSid);

        try {
            // -- Simulacion de la respuesta de Twilio REST API --
            // POST https://api.twilio.com/2010-04-01/Accounts/{SID}/Messages.json
            // Response: SID con formato SM + 32 hex chars, status "queued"
            String sid = "SM" + UUID.randomUUID().toString().replace("-", "");

            log.info("[SMS] [Twilio] SID={} | Status=queued | To={} | From=+15551234567",
                    sid, request.phoneNumber());
            log.debug(
                    "[SMS] [Twilio] Response: {{ \"sid\": \"{}\", \"status\": \"queued\", \"direction\": \"outbound-api\" }}",
                    sid);
            return new NotificationResult.Success(sid, Instant.now());

        } catch (Exception ex) {
            log.error("[SMS] Fallo en el envio | error={}", ex.getMessage(), ex);
            return new NotificationResult.Failure("SMS_SEND_ERROR", ex.getMessage());
        }
    }

    @Override
    public ChannelType getType() {
        return ChannelType.SMS;
    }
}
