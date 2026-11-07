package cl.duoc.digitalfix.workorders.service;

import cl.duoc.digitalfix.workorders.messaging.WorkOrderEventPublisher;
import cl.duoc.digitalfix.workorders.model.EstadoOrden;
import cl.duoc.digitalfix.workorders.model.WorkOrder;
import cl.duoc.digitalfix.workorders.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkOrderServiceObservacionesTest {

    private final WorkOrderRepository repository = mock(WorkOrderRepository.class);
    private final WorkOrderEventPublisher publisher = mock(WorkOrderEventPublisher.class);
    private final WorkOrderService service = new WorkOrderService(repository, publisher);

    private WorkOrder ordenEnEjecucion() {
        WorkOrder orden = new WorkOrder();
        orden.setId(122L);
        orden.setEstado(EstadoOrden.EN_EJECUCION);
        orden.setTecnicoId(5L);
        return orden;
    }

    @Test
    void noPermiteCerrarSinObservacion() {
        when(repository.findById(122L))
                .thenReturn(Optional.of(ordenEnEjecucion()));

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarEstado(122L, EstadoOrden.CERRADA, "  "));

        verify(repository, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void permiteCerrarConObservacionYPublicaEventos() {
        when(repository.findById(122L))
                .thenReturn(Optional.of(ordenEnEjecucion()));
        when(repository.save(any(WorkOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder guardada = service.actualizarEstado(
                122L,
                EstadoOrden.CERRADA,
                "  Trabajo finalizado; batería dura 30 minutos.  "
        );

        assertEquals(EstadoOrden.CERRADA, guardada.getEstado());
        assertEquals("Trabajo finalizado; batería dura 30 minutos.",
                guardada.getObservacionTecnica());

        verify(repository).save(guardada);
        verify(publisher).enviarEmail(122L, "CERRADA");
        verify(publisher).generarReporte(122L, "CERRADA");
    }

    @Test
    void rechazaObservacionQueExcedeMilCaracteres() {
        when(repository.findById(122L))
                .thenReturn(Optional.of(ordenEnEjecucion()));

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarEstado(
                        122L, EstadoOrden.CERRADA, "x".repeat(1001)));

        verify(repository, never()).save(any());
    }
}
