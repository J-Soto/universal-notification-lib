package com.novacomp.notification.demo;

import com.novacomp.notification.config.NotificationConfig;
import com.novacomp.notification.core.AsyncNotificationService;
import com.novacomp.notification.core.NotificationChannel;
import com.novacomp.notification.core.NotificationResult;
import com.novacomp.notification.core.NotificationService;
import com.novacomp.notification.channel.EmailChannel;
import com.novacomp.notification.model.EmailRequest;
import com.novacomp.notification.model.PushRequest;
import com.novacomp.notification.model.SmsRequest;
import com.novacomp.notification.resilience.RetryChannelDecorator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Aplicacion de demostracion que exhibe todas las capacidades de la libreria
 * Universal Notification Lib.
 * <p>
 * Ejecuta un recorrido completo por:
 * <ol>
 * <li>Envio sincrono por los 3 canales (Email, SMS, Push)</li>
 * <li>Envio con reintentos usando el {@link RetryChannelDecorator}</li>
 * <li>Envio asincrono con Virtual Threads</li>
 * </ol>
 */
public final class DemoApp {

    private static final String SEPARADOR = "=".repeat(60);
    private static final String LINEA = "-".repeat(60);

    public static void main(String[] args) throws Exception {
        System.out.println(SEPARADOR);
        System.out.println("  Universal Notification Lib -- Demo Interactiva");
        System.out.println("  Java 21 | Virtual Threads | Patron Decorator");
        System.out.println(SEPARADOR);
        System.out.println();

        // --------------------------------------------------------------
        // 1. Configuracion
        // --------------------------------------------------------------
        System.out.println("[PASO 1] Configurando la libreria...");
        System.out.println(LINEA);

        NotificationConfig config = NotificationConfig.builder()
                .property("email.from", "noreply@novacomp.com")
                .property("sms.provider", "twilio")
                .retryAttempts(3)
                .baseDelayMs(500L)
                .build();

        NotificationService service = new NotificationService(config);
        System.out.println("   [OK] NotificationConfig creado (retryAttempts=3, baseDelayMs=500)");
        System.out.println("   [OK] NotificationService inicializado");
        System.out.println();

        // --------------------------------------------------------------
        // 2. Envio sincrono por los 3 canales
        // --------------------------------------------------------------
        System.out.println("[PASO 2] Envio sincrono -- 3 canales (Email, SMS, Push)");
        System.out.println(LINEA);

        // Email
        var emailRequest = new EmailRequest("usuario@ejemplo.com", "Bienvenido!", "Gracias por registrarte.");
        NotificationResult emailResult = service.send(emailRequest);
        imprimirResultado("EMAIL", emailResult);

        // SMS
        var smsRequest = new SmsRequest("+506 8888-1234", "Tu codigo de verificacion es: 482913");
        NotificationResult smsResult = service.send(smsRequest);
        imprimirResultado("SMS", smsResult);

        // Push
        var pushRequest = new PushRequest("device-token-abc123", "Nueva oferta", "50% de descuento hoy!");
        NotificationResult pushResult = service.send(pushRequest);
        imprimirResultado("PUSH", pushResult);
        System.out.println();

        // --------------------------------------------------------------
        // 3. Decorador de reintentos (Patron Decorator + OCP)
        // --------------------------------------------------------------
        System.out.println("[PASO 3] Patron Decorator -- Canal con reintentos");
        System.out.println(LINEA);
        System.out.println("   Envolviendo EmailChannel con RetryChannelDecorator...");
        System.out.println("   maxRetries=3, baseDelayMs=500 (backoff exponencial)");

        NotificationChannel<EmailRequest> emailChannel = new EmailChannel(config);
        NotificationChannel<EmailRequest> canalResilienteEmail = new RetryChannelDecorator<>(emailChannel, 3, 500L);

        NotificationResult retryResult = canalResilienteEmail.send(
                new EmailRequest("admin@novacomp.com", "Reporte diario", "Adjunto el reporte."));
        imprimirResultado("EMAIL (con reintentos)", retryResult);
        System.out.println("   [INFO] El EmailChannel original NO fue modificado (OCP OK)");
        System.out.println();

        // --------------------------------------------------------------
        // 4. Envio asincrono con Virtual Threads (Java 21)
        // --------------------------------------------------------------
        System.out.println("[PASO 4] Envio asincrono -- Virtual Threads (Java 21)");
        System.out.println(LINEA);
        System.out.println("   Usando Executors.newVirtualThreadPerTaskExecutor()...");

        try (var asyncService = new AsyncNotificationService(config)) {
            CompletableFuture<NotificationResult> futureEmail = asyncService.sendAsync(
                    new EmailRequest("async@novacomp.com", "Notificacion Async", "Enviado desde un Virtual Thread."));

            System.out.println("   CompletableFuture creado (hilo principal NO bloqueado)");
            System.out.println("   Esperando resultado...");

            NotificationResult asyncResult = futureEmail.get(5, TimeUnit.SECONDS);
            imprimirResultado("EMAIL ASYNC", asyncResult);

            // Verificar que se usa Virtual Thread
            CompletableFuture<String> futureThreadInfo = CompletableFuture.supplyAsync(() -> {
                Thread t = Thread.currentThread();
                return String.format("Hilo: %s | Virtual: %s", t.getName(), t.isVirtual());
            }, java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());

            System.out.println("   [THREAD] " + futureThreadInfo.get(5, TimeUnit.SECONDS));
        }

        System.out.println();
        System.out.println(SEPARADOR);
        System.out.println("  Demo completada exitosamente");
        System.out.println("  Universal Notification Lib v1.0.0");
        System.out.println(SEPARADOR);
    }

    // ------------------------------------------------------------------
    // Helper para imprimir resultados
    // ------------------------------------------------------------------

    private static void imprimirResultado(String canal, NotificationResult result) {
        switch (result) {
            case NotificationResult.Success s ->
                System.out.printf("   [OK] [%s] Enviado -- messageId=%s, timestamp=%s%n",
                        canal, s.messageId(), s.timestamp());
            case NotificationResult.Failure f ->
                System.out.printf("   [FAIL] [%s] Fallo -- codigo=%s, razon=%s%n",
                        canal, f.code(), f.reason());
        }
    }
}
