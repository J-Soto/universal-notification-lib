package com.novacomp.notification.factory;

import com.novacomp.notification.channel.EmailChannel;
import com.novacomp.notification.channel.PushChannel;
import com.novacomp.notification.channel.SmsChannel;
import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.model.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChannelFactory -- Factory + expresion switch")
class ChannelFactoryTest {

    private NotificationConfig config;

    @BeforeEach
    void setUp() {
        config = NotificationConfig.builder().build();
    }

    @Test
    @DisplayName("crea EmailChannel para tipo EMAIL")
    void createsEmailChannel() {
        var channel = ChannelFactory.create(ChannelType.EMAIL, config);
        assertInstanceOf(EmailChannel.class, channel);
        assertEquals(ChannelType.EMAIL, channel.getType());
    }

    @Test
    @DisplayName("crea SmsChannel para tipo SMS")
    void createsSmsChannel() {
        var channel = ChannelFactory.create(ChannelType.SMS, config);
        assertInstanceOf(SmsChannel.class, channel);
        assertEquals(ChannelType.SMS, channel.getType());
    }

    @Test
    @DisplayName("crea PushChannel para tipo PUSH")
    void createsPushChannel() {
        var channel = ChannelFactory.create(ChannelType.PUSH, config);
        assertInstanceOf(PushChannel.class, channel);
        assertEquals(ChannelType.PUSH, channel.getType());
    }

    @ParameterizedTest(name = "crea un canal para {0}")
    @EnumSource(ChannelType.class)
    @DisplayName("cubre todos los valores de ChannelType")
    void coversAllTypes(ChannelType type) {
        var channel = ChannelFactory.create(type, config);
        assertNotNull(channel);
        assertEquals(type, channel.getType());
    }

    @Test
    @DisplayName("rechaza ChannelType nulo")
    void rejectsNullType() {
        assertThrows(NullPointerException.class,
                () -> ChannelFactory.create(null, config));
    }

    @Test
    @DisplayName("rechaza config nula")
    void rejectsNullConfig() {
        assertThrows(NullPointerException.class,
                () -> ChannelFactory.create(ChannelType.EMAIL, null));
    }
}
