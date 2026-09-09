package cl.duoc.digitalfix.workorders.service;

import cl.duoc.digitalfix.workorders.exception.RecursoNoEncontradoException;
import cl.duoc.digitalfix.workorders.exception.TransicionEstadoInvalidaException;
import cl.duoc.digitalfix.workorders.model.EstadoOrden;
import cl.duoc.digitalfix.workorders.model.WorkOrder;
import cl.duoc.digitalfix.workorders.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkOrderService {

    private final WorkOrderRepository repository;

    public WorkOrderService(WorkOrderRepository repository) {
        this.repository = repository;
    }

    public WorkOrder crear(WorkOrder workOrder) {

        LocalDateTime ahora = LocalDateTime.now();

        workOrder.setId(null);
        workOrder.setEstado(EstadoOrden.CREADA);
        workOrder.setFechaCreacion(ahora);
        workOrder.setFechaActualizacion(ahora);

        return repository.save(workOrder);
    }

    public List<WorkOrder> listarConFiltros(
            EstadoOrden estado,
            LocalDateTime desde,
            LocalDateTime hasta) {

        if ((desde == null && hasta != null) ||
                (desde != null && hasta == null)) {

            throw new IllegalArgumentException(
                    "Los parámetros from y to deben enviarse juntos"
            );
        }

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha from no puede ser posterior a to"
            );
        }

        if (estado != null && desde != null) {
            return repository.findByEstadoAndFechaCreacionBetween(
                    estado,
                    desde,
                    hasta
            );
        }

        if (estado != null) {
            return repository.findByEstado(estado);
        }

        if (desde != null) {
            return repository.findByFechaCreacionBetween(
                    desde,
                    hasta
            );
        }

        return repository.findAll();
    }

    public Optional<WorkOrder> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public WorkOrder actualizarEstado(
            Long id,
            EstadoOrden nuevoEstado) {

        WorkOrder workOrder = repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Orden de trabajo no encontrada"
                        )
                );

        EstadoOrden estadoActual = workOrder.getEstado();

        if (nuevoEstado == EstadoOrden.ASIGNADA &&
                workOrder.getTecnicoId() == null) {

            throw new TransicionEstadoInvalidaException(
                    "No se puede asignar la orden sin un técnico"
            );
        }

        boolean transicionValida =
                (estadoActual == EstadoOrden.CREADA &&
                        nuevoEstado == EstadoOrden.ASIGNADA) ||

                        (estadoActual == EstadoOrden.ASIGNADA &&
                                nuevoEstado == EstadoOrden.EN_DESPLAZAMIENTO) ||

                        (estadoActual == EstadoOrden.EN_DESPLAZAMIENTO &&
                                nuevoEstado == EstadoOrden.EN_EJECUCION) ||

                        (estadoActual == EstadoOrden.EN_EJECUCION &&
                                nuevoEstado == EstadoOrden.CERRADA) ||

                        (nuevoEstado == EstadoOrden.CANCELADA &&
                                estadoActual != EstadoOrden.CERRADA &&
                                estadoActual != EstadoOrden.CANCELADA);

        if (!transicionValida) {
            throw new TransicionEstadoInvalidaException(
                    "Transición de estado no permitida: "
                            + estadoActual
                            + " -> "
                            + nuevoEstado
            );
        }

        workOrder.setEstado(nuevoEstado);
        workOrder.setFechaActualizacion(LocalDateTime.now());

        return repository.save(workOrder);
    }
}