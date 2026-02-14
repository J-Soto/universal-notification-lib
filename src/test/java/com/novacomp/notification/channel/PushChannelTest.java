package com.novacomp.notification.channel;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.core.NotificationResult;
import com.novacomp.notification.model.ChannelType;
import com.novacomp.notification.model.PushRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PushChannel -- Implementacion Strategy")
class PushChannelTest {

    private PushChannel channel;

    @BeforeEach
    void setUp() {
        var config = NotificationConfig.builder().build();
        channel = new PushChannel(config);
    }

    @Test
    @DisplayName("debe retornar tipo PUSH")
    void returnsCorrectType() {
        assertEquals(ChannelType.PUSH, channel.getType());
    }

    @Test
    @DisplayName("send() retorna Success con messageId")
    void sendReturnsSuccess() {
        var request = new PushRequest("device-xyz", "Alerta", "Algo sucedió");
        var result = channel.send(request);

        assertInstanceOf(NotificationResult.Success.class, result);
        var success = (NotificationResult.Success) result;
        assertNotNull(success.messageId());
        assertNotNull(success.timestamp());
    }
}
