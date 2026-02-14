package com.novacomp.notification.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuración inmutable y programática para la librería de notificaciones.
 * <p>
 * Se construye exclusivamente mediante el {@link Builder} fluido -- sin YAML,
 * sin {@code .properties}, sin archivos externos.
 *
 * <pre>{@code
 * NotificationConfig config = NotificationConfig.builder()
 *         .property("email.from", "noreply@novacomp.com")
 *         .property("sms.provider", "twilio")
 *         .retryAttempts(3)
 *         .baseDelayMs(2000L)
 *         .build();
 * }</pre>
 */
public final class NotificationConfig {

    private final Map<String, String> properties;
    private final int retryAttempts;
    private final long baseDelayMs;

    private NotificationConfig(Builder builder) {
        this.properties = Collections.unmodifiableMap(new HashMap<>(builder.properties));
        this.retryAttempts = builder.retryAttempts;
        this.baseDelayMs = builder.baseDelayMs;
    }

    /**
     * Obtiene una propiedad de configuración por clave.
     *
     * @param key la clave de la propiedad
     * @return el valor de la propiedad, o {@code null} si no está configurada
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Obtiene una propiedad de configuración por clave, retornando un valor
     * por defecto si la clave no está presente.
     *
     * @param key          la clave de la propiedad
     * @param defaultValue valor retornado cuando la clave está ausente
     * @return el valor de la propiedad o {@code defaultValue}
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /**
     * Retorna el número de reintentos configurados para envíos fallidos.
     *
     * @return número de reintentos (≥ 0)
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * Retorna el delay base en milisegundos para el backoff exponencial.
     * <p>
     * Delay real por intento: {@code baseDelayMs x 2^numeroDeIntento}.
     *
     * @return delay base en ms (≥ 1)
     */
    public long getBaseDelayMs() {
        return baseDelayMs;
    }

    /**
     * Retorna una vista no modificable de todas las propiedades de configuración.
     *
     * @return mapa inmutable de propiedades
     */
    public Map<String, String> getAllProperties() {
        return properties;
    }

    /**
     * Crea una nueva instancia de {@link Builder}.
     *
     * @return un nuevo builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // ------------------------------------------------------------------ //
    // Builder
    // ------------------------------------------------------------------ //

    /**
     * Builder fluido para {@link NotificationConfig}.
     */
    public static final class Builder {

        private final Map<String, String> properties = new HashMap<>();
        private int retryAttempts = 3;
        private long baseDelayMs = 1000L;

        private Builder() {
        }

        /**
         * Agrega una propiedad de configuración clave-valor.
         *
         * @param key   clave de la propiedad (no puede ser {@code null})
         * @param value valor de la propiedad (no puede ser {@code null})
         * @return este builder
         */
        public Builder property(String key, String value) {
            Objects.requireNonNull(key, "La clave de la propiedad no puede ser nula");
            Objects.requireNonNull(value, "El valor de la propiedad no puede ser nulo");
            this.properties.put(key, value);
            return this;
        }

        /**
         * Agrega todas las entradas del mapa proporcionado como propiedades.
         *
         * @param properties mapa de propiedades (no puede ser {@code null})
         * @return este builder
         */
        public Builder properties(Map<String, String> properties) {
            Objects.requireNonNull(properties, "El mapa de propiedades no puede ser nulo");
            this.properties.putAll(properties);
            return this;
        }

        /**
         * Establece el número de reintentos para envíos fallidos.
         *
         * @param retryAttempts número de reintentos (debe ser ≥ 0)
         * @return este builder
         */
        public Builder retryAttempts(int retryAttempts) {
            if (retryAttempts < 0) {
                throw new IllegalArgumentException("Los reintentos deben ser >= 0");
            }
            this.retryAttempts = retryAttempts;
            return this;
        }

        /**
         * Establece el delay base en milisegundos para el backoff exponencial.
         *
         * @param baseDelayMs delay base (debe ser ≥ 1)
         * @return este builder
         */
        public Builder baseDelayMs(long baseDelayMs) {
            if (baseDelayMs < 1) {
                throw new IllegalArgumentException("El delay base debe ser >= 1 ms");
            }
            this.baseDelayMs = baseDelayMs;
            return this;
        }

        /**
         * Construye una instancia inmutable de {@link NotificationConfig}
         * a partir del estado actual del builder.
         *
         * @return la instancia de configuración
         */
        public NotificationConfig build() {
            return new NotificationConfig(this);
        }
    }
}
