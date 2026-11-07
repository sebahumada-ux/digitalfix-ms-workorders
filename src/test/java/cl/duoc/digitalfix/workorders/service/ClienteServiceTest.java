package cl.duoc.digitalfix.workorders.service;

import cl.duoc.digitalfix.workorders.model.Cliente;
import cl.duoc.digitalfix.workorders.repository.ClienteRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    private final ClienteRepository repository = mock(ClienteRepository.class);
    private final ClienteService service = new ClienteService(repository);

    @Test
    void buscaClientesPorNombreSinExigirRecordarId() {
        Cliente cliente = new Cliente();
        cliente.setId(42L);
        cliente.setNombre("María González");

        when(repository.findTop20ByNombreContainingIgnoreCaseOrderByNombreAsc("María"))
                .thenReturn(List.of(cliente));

        List<Cliente> encontrados = service.buscar("  María  ");

        assertEquals(1, encontrados.size());
        assertEquals(42L, encontrados.get(0).getId());
        verify(repository).findTop20ByNombreContainingIgnoreCaseOrderByNombreAsc("María");
    }

    @Test
    void noRegistraNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> service.crear("  "));
        verifyNoInteractions(repository);
    }
}
