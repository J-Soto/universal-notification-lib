package com.novacomp.notification.model;

/**
 * Enumeración de canales de notificación soportados.
 * <p>
 * Agregar un nuevo valor aquí forzará un error en tiempo de compilación
 * en cada expresión {@code switch} exhaustiva de la librería, asegurando
 * que los nuevos canales sean manejados en todas partes.
 */
public enum ChannelType {

    EMAIL,
    SMS,
    PUSH
}
