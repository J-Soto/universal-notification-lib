package com.novacomp.notification.core;

import com.novacomp.notification.model.ChannelType;

/**
 * Interfaz Strategy para enviar notificaciones a través de un canal específico.
 * <p>
 * Cada implementación maneja un único {@link ChannelType} y acepta
 * un objeto de solicitud fuertemente tipado {@code T} (ej:
 * {@code EmailRequest},
 * {@code SmsRequest}, {@code PushRequest}).
 * <p>
 * Las implementaciones pueden intercambiarse de forma transparente -- por
 * ejemplo,
 * reemplazar un canal de email stub por uno respaldado por SendGrid -- sin
 * modificar el código del cliente.
 *
 * @param <T> el tipo de solicitud de notificación que este canal maneja
 */
public interface NotificationChannel<T> {

    /**
     * Envía una notificación a través de este canal.
     *
     * @param request la carga útil de la solicitud de notificación
     * @return un {@link NotificationResult} -- ya sea
     *         {@link NotificationResult.Success} o
     *         {@link NotificationResult.Failure}
     */
    NotificationResult send(T request);

    /**
     * Retorna el {@link ChannelType} que esta implementación maneja.
     *
     * @return el tipo de canal
     */
    ChannelType getType();
}
