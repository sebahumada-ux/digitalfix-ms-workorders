package cl.duoc.digitalfix.workorders.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CLIENTES_DIGITALFIX")
@Data
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    @Column(name = "NOMBRE", nullable = false, length = 120)
    private String nombre;
}
