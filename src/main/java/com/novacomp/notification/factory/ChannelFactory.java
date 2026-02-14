package com.novacomp.notification.factory;

import com.novacomp.notification.channel.EmailChannel;
import com.novacomp.notification.channel.PushChannel;
import com.novacomp.notification.channel.SmsChannel;
import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.core.NotificationChannel;
import com.novacomp.notification.model.ChannelType;

import java.util.Objects;

/**
 * Factory que crea la implementación apropiada de {@link NotificationChannel}
 * basándose en un {@link ChannelType}.
 * <p>
 * Utiliza una <strong>expresión switch de Java 21</strong> sobre el enum, que
 * el compilador verifica como exhaustiva. Agregar un nuevo valor a
 * {@code ChannelType} producirá un error en tiempo de compilación aquí
 * hasta que se agregue la rama correspondiente -- riesgo cero de olvidar
 * silenciosamente un canal.
 */
public final class ChannelFactory {

    private ChannelFactory() {
        // Clase utilitaria -- no instanciable
    }

    /**
     * Crea una implementación de canal para el tipo dado.
     *
     * @param type   el tipo de canal a instanciar
     * @param config configuración compartida de la librería
     * @return una instancia de {@link NotificationChannel} lista para usar
     * @throws NullPointerException si algún argumento es {@code null}
     */
    public static NotificationChannel<?> create(ChannelType type,
            NotificationConfig config) {
        Objects.requireNonNull(type, "El ChannelType no puede ser nulo");
        Objects.requireNonNull(config, "La NotificationConfig no puede ser nula");

        return switch (type) {
            case EMAIL -> new EmailChannel(config);
            case SMS -> new SmsChannel(config);
            case PUSH -> new PushChannel(config);
        };
    }
}
