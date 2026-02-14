package com.novacomp.notification.channel;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.core.NotificationResult;
import com.novacomp.notification.model.ChannelType;
import com.novacomp.notification.model.SmsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SmsChannel -- Implementacion Strategy")
class SmsChannelTest {

    private SmsChannel channel;

    @BeforeEach
    void setUp() {
        var config = NotificationConfig.builder()
                .property("sms.provider", "twilio")
                .build();
        channel = new SmsChannel(config);
    }

    @Test
    @DisplayName("debe retornar tipo SMS")
    void returnsCorrectType() {
        assertEquals(ChannelType.SMS, channel.getType());
    }

    @Test
    @DisplayName("send() retorna Success con messageId")
    void sendReturnsSuccess() {
        var request = new SmsRequest("+1234567890", "Hola SMS");
        var result = channel.send(request);

        assertInstanceOf(NotificationResult.Success.class, result);
        var success = (NotificationResult.Success) result;
        assertNotNull(success.messageId());
        assertNotNull(success.timestamp());
    }
}
