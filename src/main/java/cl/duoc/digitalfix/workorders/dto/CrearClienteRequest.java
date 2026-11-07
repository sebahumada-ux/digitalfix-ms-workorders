package cl.duoc.digitalfix.workorders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearClienteRequest(
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre
) {
}
