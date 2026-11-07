package cl.duoc.digitalfix.workorders.dto;

import cl.duoc.digitalfix.workorders.model.EstadoOrden;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarEstadoRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoOrden estado;

    @Size(max = 1000, message = "La observación no puede superar 1000 caracteres")
    private String observacionTecnica;
}