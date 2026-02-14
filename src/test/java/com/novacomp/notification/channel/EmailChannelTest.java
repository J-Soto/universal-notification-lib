package com.novacomp.notification.channel;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.core.NotificationResult;
import com.novacomp.notification.model.ChannelType;
import com.novacomp.notification.model.EmailRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailChannel -- Implementacion Strategy")
class EmailChannelTest {

    private EmailChannel channel;

    @BeforeEach
    void setUp() {
        var config = NotificationConfig.builder()
                .property("email.from", "noreply@test.com")
                .build();
        channel = new EmailChannel(config);
    }

    @Test
    @DisplayName("debe retornar tipo EMAIL")
    void returnsCorrectType() {
        assertEquals(ChannelType.EMAIL, channel.getType());
    }

    @Test
    @DisplayName("send() retorna Success con messageId")
    void sendReturnsSuccess() {
        var request = new EmailRequest("user@example.com", "Asunto", "Cuerpo");
        var result = channel.send(request);

        assertInstanceOf(NotificationResult.Success.class, result);
        var success = (NotificationResult.Success) result;
        assertNotNull(success.messageId());
        assertNotNull(success.timestamp());
    }
}
