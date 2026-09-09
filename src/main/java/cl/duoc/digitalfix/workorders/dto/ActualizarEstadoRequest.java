package cl.duoc.digitalfix.workorders.dto;

import cl.duoc.digitalfix.workorders.model.EstadoOrden;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarEstadoRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoOrden estado;
}