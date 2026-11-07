package cl.duoc.digitalfix.workorders.repository;

import cl.duoc.digitalfix.workorders.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findTop20ByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    List<Cliente> findTop20ByOrderByNombreAsc();
}
