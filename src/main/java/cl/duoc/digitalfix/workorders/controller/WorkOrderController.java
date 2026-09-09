package cl.duoc.digitalfix.workorders.controller;

import cl.duoc.digitalfix.workorders.dto.ActualizarEstadoRequest;
import cl.duoc.digitalfix.workorders.model.EstadoOrden;
import cl.duoc.digitalfix.workorders.model.WorkOrder;
import cl.duoc.digitalfix.workorders.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/workorders")
public class WorkOrderController {

    private final WorkOrderService service;

    public WorkOrderController(WorkOrderService service) {
        this.service = service;
    }

    // Crear una nueva orden de trabajo
    @PostMapping
    public ResponseEntity<WorkOrder> crear(
            @Valid @RequestBody WorkOrder workOrder) {

        WorkOrder nuevaOrden = service.crear(workOrder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevaOrden);
    }

    // Listar órdenes y permitir filtros por estado y rango de fechas
    @GetMapping
    public ResponseEntity<List<WorkOrder>> listar(
            @RequestParam(required = false, name = "status")
            EstadoOrden estado,

            @RequestParam(required = false, name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime desde,

            @RequestParam(required = false, name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime hasta) {

        List<WorkOrder> ordenes =
                service.listarConFiltros(estado, desde, hasta);

        return ResponseEntity.ok(ordenes);
    }

    // Buscar una orden por su ID
    @GetMapping("/{id}")
    public ResponseEntity<WorkOrder> buscarPorId(
            @PathVariable Long id) {

        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Actualizar el estado de una orden
    @PutMapping("/{id}/status")
    public ResponseEntity<WorkOrder> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoRequest request) {

        WorkOrder ordenActualizada =
                service.actualizarEstado(id, request.getEstado());

        return ResponseEntity.ok(ordenActualizada);
    }
}