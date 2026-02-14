package com.novacomp.notification.resilience;

import com.novacomp.notification.core.NotificationChannel;
import com.novacomp.notification.core.NotificationResult;
import com.novacomp.notification.model.ChannelType;
import com.novacomp.notification.model.EmailRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests TDD para {@link RetryChannelDecorator}.
 * <p>
 * Usa Mockito para simular el comportamiento del canal: fallos configurables
 * seguidos de un éxito opcional, verificando el conteo de reintentos y el
 * backoff exponencial sin dormir realmente (backoff configurado a 1 ms en
 * tests).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetryChannelDecorator -- Reintento con Backoff Exponencial")
class RetryChannelDecoratorTest {

    private static final EmailRequest SOLICITUD_EJEMPLO = new EmailRequest("user@test.com", "Asunto", "Cuerpo");

    private static final NotificationResult EXITO = new NotificationResult.Success("msg-123", Instant.now());

    private static final NotificationResult FALLO = new NotificationResult.Failure("SEND_ERROR", "Conexión rechazada");

    @Mock
    private NotificationChannel<EmailRequest> mockChannel;

    // ------------------------------------------------------------------ //
    // Camino feliz
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("tiene exito en el primer intento -- no necesita reintentos")
    void succeedsOnFirstAttempt() {
        when(mockChannel.send(SOLICITUD_EJEMPLO)).thenReturn(EXITO);

        var decorator = new RetryChannelDecorator<>(mockChannel, 3, 1L);
        var result = decorator.send(SOLICITUD_EJEMPLO);

        assertInstanceOf(NotificationResult.Success.class, result);
        verify(mockChannel, times(1)).send(SOLICITUD_EJEMPLO);
    }

    // ------------------------------------------------------------------ //
    // Lógica principal de reintentos
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("falla 2 veces y tiene exito en el 3er intento -- retorna Success")
    void retriesAndSucceedsOnThirdAttempt() {
        when(mockChannel.send(SOLICITUD_EJEMPLO))
                .thenReturn(FALLO) // 1er intento -- falla
                .thenReturn(FALLO) // 2do intento -- falla (1er reintento)
                .thenReturn(EXITO); // 3er intento -- exito (2do reintento)

        var decorator = new RetryChannelDecorator<>(mockChannel, 3, 1L);
        var result = decorator.send(SOLICITUD_EJEMPLO);

        assertInstanceOf(NotificationResult.Success.class, result);
        verify(mockChannel, times(3)).send(SOLICITUD_EJEMPLO);
    }

    // ------------------------------------------------------------------ //
    // Todos los reintentos agotados
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("falla en todos los intentos -- retorna el ultimo Failure")
    void returnsLastFailureWhenAllRetriesExhausted() {
        var ultimoFallo = new NotificationResult.Failure("TIMEOUT", "Timeout del gateway");

        when(mockChannel.send(SOLICITUD_EJEMPLO))
                .thenReturn(FALLO) // 1er intento
                .thenReturn(FALLO) // 1er reintento
                .thenReturn(FALLO) // 2do reintento
                .thenReturn(ultimoFallo); // 3er reintento (último)

        var decorator = new RetryChannelDecorator<>(mockChannel, 3, 1L);
        var result = decorator.send(SOLICITUD_EJEMPLO);

        assertInstanceOf(NotificationResult.Failure.class, result);
        var failure = (NotificationResult.Failure) result;
        assertEquals("TIMEOUT", failure.code());
        assertEquals("Timeout del gateway", failure.reason());

        // 1 inicial + 3 reintentos = 4 invocaciones totales
        verify(mockChannel, times(4)).send(SOLICITUD_EJEMPLO);
    }

    // ------------------------------------------------------------------ //
    // Transparencia del Decorator
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("delega getType() al canal envuelto")
    void delegatesGetType() {
        when(mockChannel.getType()).thenReturn(ChannelType.EMAIL);

        var decorator = new RetryChannelDecorator<>(mockChannel, 3, 1L);

        assertEquals(ChannelType.EMAIL, decorator.getType());
        verify(mockChannel).getType();
    }
}
