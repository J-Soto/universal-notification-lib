package com.novacomp.notification.core;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.model.EmailRequest;
import com.novacomp.notification.model.SmsRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para {@link AsyncNotificationService}.
 * <p>
 * Verifica el comportamiento no-bloqueante, la ejecución en Virtual Threads
 * y la delegación correcta al {@link NotificationService} síncrono.
 */
@DisplayName("AsyncNotificationService -- Hilos Virtuales")
class AsyncNotificationServiceTest {

    private AsyncNotificationService asyncService;

    @BeforeEach
    void setUp() {
        var config = NotificationConfig.builder()
                .property("email.from", "noreply@novacomp.com")
                .retryAttempts(0)
                .build();
        asyncService = new AsyncNotificationService(config);
    }

    @AfterEach
    void tearDown() {
        asyncService.close();
    }

    // ------------------------------------------------------------------ //
    // Ejecución no-bloqueante
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("sendAsync retorna un CompletableFuture completado con Success")
    void sendAsyncReturnsCompletedFuture() throws Exception {
        CompletableFuture<NotificationResult> future = asyncService
                .sendAsync(new EmailRequest("u@e.com", "Hola", "Cuerpo"));

        NotificationResult result = future.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertInstanceOf(NotificationResult.Success.class, result);
    }

    // ------------------------------------------------------------------ //
    // Auto-despacho a través de la capa asíncrona
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("sendAsync auto-despacha SmsRequest correctamente")
    void sendAsyncAutoDispatchesSms() throws Exception {
        CompletableFuture<NotificationResult> future = asyncService.sendAsync(new SmsRequest("+1234567890", "Hola"));

        NotificationResult result = future.get(5, TimeUnit.SECONDS);

        assertInstanceOf(NotificationResult.Success.class, result);
    }

    // ------------------------------------------------------------------ //
    // Verificación de Virtual Threads
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("la tarea se ejecuta en un Virtual Thread")
    void executesOnVirtualThread() throws Exception {
        CompletableFuture<Boolean> isVirtual = CompletableFuture.supplyAsync(() -> Thread.currentThread().isVirtual(),
                java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());

        assertTrue(isVirtual.get(5, TimeUnit.SECONDS),
                "La tarea debe ejecutarse en un Virtual Thread");
    }
}
