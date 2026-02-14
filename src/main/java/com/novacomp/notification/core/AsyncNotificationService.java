package com.novacomp.notification.core;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.model.ChannelType;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fachada asíncrona para enviar notificaciones sobre
 * <strong>Virtual Threads de Java 21</strong>.
 * <p>
 * Envuelve el {@link NotificationService} síncrono y despacha cada
 * operación de envío a un Virtual Thread ligero, retornando un
 * {@link CompletableFuture} para que el llamador nunca se bloquee.
 *
 * <h3>¿Por qué Virtual Threads?</h3>
 * El envio de notificaciones es fundamentalmente <em>I/O-bound</em> --
 * conexiones
 * SMTP, llamadas HTTP a gateways de SMS/push, etc. Los platform threads
 * pasan la mayor parte del tiempo estacionados leyendo sockets. Los Virtual
 * Threads:
 * <ul>
 * <li><b>Escalan a millones</b> de tareas concurrentes sin la sobrecarga
 * de memoria de los platform threads (~1 KB vs ~1 MB por hilo)</li>
 * <li><b>Se desmontan del carrier thread</b> durante I/O bloqueante,
 * liberando el carrier para ejecutar otros virtual threads</li>
 * <li><b>Usan la API bloqueante familiar</b> ({@code Thread.sleep},
 * {@code InputStream.read}) en vez de frameworks async basados en
 * callbacks</li>
 * </ul>
 * Esto significa que el {@code Thread.sleep()} del backoff en el decorator
 * de reintentos es <em>gratis</em> en términos de utilización del carrier
 * thread.
 *
 * <h3>Ejemplo de uso</h3>
 * 
 * <pre>{@code
 * try (var asyncService = new AsyncNotificationService(config)) {
 *     CompletableFuture<NotificationResult> future = asyncService
 *             .sendAsync(new EmailRequest("u@e.com", "Hola", "Cuerpo"));
 *
 *     future.thenAccept(result -> switch (result) {
 *         case NotificationResult.Success s -> System.out.println("Enviado: " + s.messageId());
 *         case NotificationResult.Failure f -> System.err.println("Error: " + f.reason());
 *     });
 * }
 * }</pre>
 */
@Slf4j
public final class AsyncNotificationService implements AutoCloseable {

    private final NotificationService notificationService;
    private final ExecutorService virtualThreadExecutor;

    /**
     * Crea un servicio asíncrono respaldado por la configuración dada.
     * <p>
     * Internamente crea un {@link NotificationService} y un
     * executor de Virtual-Thread-por-tarea.
     *
     * @param config configuración de la librería (no puede ser {@code null})
     */
    public AsyncNotificationService(NotificationConfig config) {
        Objects.requireNonNull(config, "La NotificationConfig no puede ser nula");
        this.notificationService = new NotificationService(config);
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        log.info("Servicio asíncrono de notificaciones inicializado con executor de Virtual Threads");
    }

    /**
     * Envía una notificación de forma asíncrona, resolviendo automáticamente
     * el canal a partir del tipo de solicitud.
     *
     * @param request uno de {@code EmailRequest}, {@code SmsRequest},
     *                o {@code PushRequest}
     * @return un future que se completa con el {@link NotificationResult}
     */
    public CompletableFuture<NotificationResult> sendAsync(Object request) {
        Objects.requireNonNull(request, "La solicitud no puede ser nula");
        return CompletableFuture.supplyAsync(
                () -> notificationService.send(request),
                virtualThreadExecutor);
    }

    /**
     * Envía una notificación de forma asíncrona a través del canal especificado.
     *
     * @param type    el canal a utilizar
     * @param request la carga útil de la solicitud de notificación
     * @return un future que se completa con el {@link NotificationResult}
     */
    public CompletableFuture<NotificationResult> sendAsync(ChannelType type, Object request) {
        Objects.requireNonNull(type, "El ChannelType no puede ser nulo");
        Objects.requireNonNull(request, "La solicitud no puede ser nula");
        return CompletableFuture.supplyAsync(
                () -> notificationService.send(type, request),
                virtualThreadExecutor);
    }

    /**
     * Cierra el executor de Virtual Threads.
     */
    @Override
    public void close() {
        virtualThreadExecutor.close();
        log.info("Servicio asíncrono de notificaciones cerrado");
    }
}
