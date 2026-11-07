package cl.duoc.digitalfix.workorders.service;

import cl.duoc.digitalfix.workorders.model.Cliente;
import cl.duoc.digitalfix.workorders.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> buscar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return repository.findTop20ByOrderByNombreAsc();
        }
        return repository.findTop20ByNombreContainingIgnoreCaseOrderByNombreAsc(
                nombre.trim()
        );
    }

    public Cliente crear(String nombre) {
        String normalizado = nombre == null ? "" : nombre.trim();
        if (normalizado.isBlank() || normalizado.length() > 120) {
            throw new IllegalArgumentException("Nombre del cliente inválido");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(normalizado);
        return repository.save(cliente);
    }
}
