
package cl.duoc.digitalfix.workorders.dto;

public record OrdenEvento(
        String eventId,
        Long workOrderId,
        Long clienteId,
        String estado,
        Long tecnicoId,
        String timestamp
) {
}
