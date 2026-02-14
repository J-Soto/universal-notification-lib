package com.novacomp.notification.resilience;

import com.novacomp.notification.core.NotificationChannel;
import com.novacomp.notification.core.NotificationResult;
import com.novacomp.notification.model.ChannelType;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Decorator que agrega comportamiento de reintento con backoff exponencial
 * a cualquier {@link NotificationChannel}.
 * <p>
 * <b>Patron de diseno:</b> <em>Decorator</em> -- envuelve un canal existente
 * para extender su funcionalidad <em>sin modificarlo</em>, satisfaciendo
 * el <b>Principio Abierto/Cerrado (OCP)</b>.
 * <p>
 * Cuando el canal envuelto retorna un {@link NotificationResult.Failure},
 * este decorator reintenta el envío hasta {@code maxRetries} veces
 * adicionales, esperando un delay exponencialmente creciente entre intentos:
 * 
 * <pre>
 *   delay = baseDelayMs x 2^numeroDeIntento   (intento 0, 1, 2, ...)
 * </pre>
 * 
 * Si todos los intentos fallan, se retorna el <em>último</em>
 * {@link NotificationResult.Failure} al llamador.
 *
 * <h3>Ejemplo de uso</h3>
 * 
 * <pre>{@code
 * NotificationChannel<EmailRequest> email = new EmailChannel(config);
 * NotificationChannel<EmailRequest> resiliente = new RetryChannelDecorator<>(email, 3, 1000L);
 *
 * NotificationResult result = resiliente.send(request);
 * }</pre>
 *
 * @param <T> el tipo de solicitud de notificación que maneja el canal envuelto
 */
@Slf4j
public final class RetryChannelDecorator<T> implements NotificationChannel<T> {

    private final NotificationChannel<T> delegate;
    private final int maxRetries;
    private final long baseDelayMs;

    /**
     * Crea un decorator de reintentos envolviendo el canal proporcionado.
     *
     * @param delegate    el canal a decorar (no puede ser {@code null})
     * @param maxRetries  número máximo de intentos <em>adicionales</em> después
     *                    del primer fallo (debe ser ≥ 0)
     * @param baseDelayMs delay base en milisegundos para el backoff
     *                    exponencial (debe ser ≥ 1)
     */
    public RetryChannelDecorator(NotificationChannel<T> delegate,
            int maxRetries,
            long baseDelayMs) {
        this.delegate = Objects.requireNonNull(delegate, "El canal delegado no puede ser nulo");
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries debe ser >= 0");
        }
        if (baseDelayMs < 1) {
            throw new IllegalArgumentException("baseDelayMs debe ser >= 1");
        }
        this.maxRetries = maxRetries;
        this.baseDelayMs = baseDelayMs;
    }

    /**
     * Envía la solicitud a través del canal envuelto, reintentando en caso
     * de fallo con backoff exponencial.
     *
     * @param request la carga útil de la solicitud de notificación
     * @return un {@link NotificationResult} -- {@code Success} si algun intento
     *         tiene éxito, o el último {@code Failure} cuando se agotan los
     *         reintentos
     */
    @Override
    public NotificationResult send(T request) {
        NotificationResult lastResult = delegate.send(request);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            if (lastResult instanceof NotificationResult.Success) {
                return lastResult;
            }

            long delay = calculateDelay(attempt - 1);
            log.warn("[RETRY] [{}] Intento {}/{} fallo -- reintentando en {} ms",
                    getType(), attempt, maxRetries, delay);

            sleep(delay);
            lastResult = delegate.send(request);
        }

        if (lastResult instanceof NotificationResult.Failure f) {
            log.error("[FAIL] [{}] Todos los {} reintentos agotados -- ultimo error: [codigo={}, razon={}]",
                    getType(), maxRetries, f.code(), f.reason());
        }

        return lastResult;
    }

    @Override
    public ChannelType getType() {
        return delegate.getType();
    }

    // ------------------------------------------------------------------ //
    // Helpers internos
    // ------------------------------------------------------------------ //

    /**
     * Calcula el delay para el intento dado usando backoff exponencial.
     *
     * @param attemptIndex índice del intento (base cero)
     * @return delay en milisegundos
     */
    private long calculateDelay(int attemptIndex) {
        return baseDelayMs * (1L << attemptIndex); // baseDelayMs x 2^attemptIndex
    }

    /**
     * Duerme por la duración especificada. Extraído para testabilidad y
     * porque los Virtual Threads hacen que {@code Thread.sleep()} sea
     * extremadamente económico.
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep de reintento interrumpido");
        }
    }
}
