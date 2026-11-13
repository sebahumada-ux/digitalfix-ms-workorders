
package cl.duoc.digitalfix.workorders.service;

import cl.duoc.digitalfix.workorders.exception.RecursoNoEncontradoException;
import cl.duoc.digitalfix.workorders.exception.TransicionEstadoInvalidaException;
import cl.duoc.digitalfix.workorders.messaging.WorkOrderEventPublisher;
import cl.duoc.digitalfix.workorders.messaging.KafkaWorkOrderEventPublisher;
import cl.duoc.digitalfix.workorders.model.EstadoOrden;
import cl.duoc.digitalfix.workorders.model.WorkOrder;
import cl.duoc.digitalfix.workorders.repository.WorkOrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class WorkOrderService {

    private static final ZoneId ZONA_CHILE =
            ZoneId.of("America/Santiago");

    private final WorkOrderRepository repository;
    private final WorkOrderEventPublisher eventPublisher;

    // NUEVO: Productor Kafka opcional
    private KafkaWorkOrderEventPublisher kafkaPublisher;

    public WorkOrderService(
            WorkOrderRepository repository,
            WorkOrderEventPublisher eventPublisher) {

        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    // NUEVO: Inyectar Kafka solo cuando esté habilitado
    @Autowired(required = false)
    public void configurarKafkaPublisher(
            KafkaWorkOrderEventPublisher kafkaPublisher) {

        this.kafkaPublisher = kafkaPublisher;
    }

    // NUEVO: Publicar evento si Kafka está habilitado
    private void publicarEnKafka(WorkOrder orden) {

        if (kafkaPublisher != null) {
            kafkaPublisher.publicar(orden);
        }
    }

    private LocalDateTime ahoraChile() {
        return LocalDateTime.now(ZONA_CHILE);
    }

    public WorkOrder crear(WorkOrder workOrder) {

        LocalDateTime ahora = ahoraChile();

        workOrder.setId(null);
        workOrder.setEstado(EstadoOrden.CREADA);
        workOrder.setFechaCreacion(ahora);
        workOrder.setFechaActualizacion(ahora);
        workOrder.setObservacionTecnica(null);

        WorkOrder guardada = repository.save(workOrder);

        // NUEVO: Evento de creación hacia Kafka
        publicarEnKafka(guardada);

        return guardada;
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
            EstadoOrden nuevoEstado,
            String observacionTecnica) {

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

        String observacion = observacionTecnica == null
                ? ""
                : observacionTecnica.trim();

        if (observacion.length() > 1000) {
            throw new IllegalArgumentException(
                    "La observación no puede superar 1000 caracteres"
            );
        }

        if (nuevoEstado == EstadoOrden.CERRADA && observacion.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes ingresar una observación para cerrar la orden"
            );
        }

        workOrder.setEstado(nuevoEstado);
        workOrder.setFechaActualizacion(ahoraChile());

        if (!observacion.isBlank()) {
            workOrder.setObservacionTecnica(observacion);
        }

        WorkOrder guardada = repository.save(workOrder);

        // RABBITMQ: Toda la lógica original se mantiene
        switch (nuevoEstado) {

            case ASIGNADA -> {
                eventPublisher.enviarEmail(
                        guardada.getId(),
                        nuevoEstado.name()
                );

                eventPublisher.enviarDespacho(
                        guardada.getId(),
                        nuevoEstado.name()
                );
            }

            case EN_DESPLAZAMIENTO -> {
                eventPublisher.enviarEmail(
                        guardada.getId(),
                        nuevoEstado.name()
                );
            }

            case CERRADA -> {
                eventPublisher.enviarEmail(
                        guardada.getId(),
                        nuevoEstado.name()
                );

                eventPublisher.generarReporte(
                        guardada.getId(),
                        nuevoEstado.name()
                );
            }

            default -> {
            }
        }

        // NUEVO: Publicar el cambio de estado en Kafka
        publicarEnKafka(guardada);

        return guardada;
    }
}
