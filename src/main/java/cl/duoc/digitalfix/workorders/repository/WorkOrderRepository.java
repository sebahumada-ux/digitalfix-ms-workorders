package cl.duoc.digitalfix.workorders.repository;

import cl.duoc.digitalfix.workorders.model.EstadoOrden;
import cl.duoc.digitalfix.workorders.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    List<WorkOrder> findByEstado(EstadoOrden estado);

    List<WorkOrder> findByFechaCreacionBetween(
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<WorkOrder> findByEstadoAndFechaCreacionBetween(
            EstadoOrden estado,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}