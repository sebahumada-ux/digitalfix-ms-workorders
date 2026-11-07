package cl.duoc.digitalfix.workorders.controller;

import cl.duoc.digitalfix.workorders.dto.CrearClienteRequest;
import cl.duoc.digitalfix.workorders.model.Cliente;
import cl.duoc.digitalfix.workorders.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> buscar(
            @RequestParam(required = false) String nombre) {

        return ResponseEntity.ok(service.buscar(nombre));
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(
            @Valid @RequestBody CrearClienteRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crear(request.nombre()));
    }
}
