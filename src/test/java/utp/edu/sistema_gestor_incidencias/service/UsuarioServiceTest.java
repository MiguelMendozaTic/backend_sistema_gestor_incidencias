package utp.edu.sistema_gestor_incidencias.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.RoleRepository;
import utp.edu.sistema_gestor_incidencias.repository.UsuarioRepository;

// "Mi perfil": el usuario solo cambia su nombre; usuario y correo son del admin
class UsuarioServiceTest {

  private UsuarioRepository usuarioRepository;
  private UsuarioService service;
  private Usuario yo;

  @BeforeEach
  void setUp() {
    usuarioRepository = mock(UsuarioRepository.class);
    service = new UsuarioService(usuarioRepository, mock(RoleRepository.class));

    yo = new Usuario();
    yo.setId(1L);
    yo.setUsername("ana");
    yo.setNombre("Ana Torres");
    yo.setCorreo("ana@empresa.com");

    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("ana", null, "ROLE_EMPLEADO"));
    when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(yo));
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @AfterEach
  void limpiar() {
    SecurityContextHolder.clearContext();
  }

  private static Usuario datos(String username, String nombre, String correo) {
    Usuario u = new Usuario();
    u.setUsername(username);
    u.setNombre(nombre);
    u.setCorreo(correo);
    return u;
  }

  @Test
  void perfil_cambiaSoloElNombre() {
    Usuario r = service.actualizarPerfil(datos("ana", "  Ana Lucía Torres ", "ana@empresa.com"));

    assertEquals("Ana Lucía Torres", r.getNombre());
    assertEquals("ana", r.getUsername());
    assertEquals("ana@empresa.com", r.getCorreo());
  }

  @Test
  void perfil_noPuedeCambiarSuUsuario() {
    assertThrows(IllegalArgumentException.class,
        () -> service.actualizarPerfil(datos("ana2", "Ana Torres", "ana@empresa.com")));
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void perfil_noPuedeCambiarSuCorreo() {
    assertThrows(IllegalArgumentException.class,
        () -> service.actualizarPerfil(datos("ana", "Ana Torres", "otro@empresa.com")));
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void perfil_nombreDemasiadoCorto_rechazado() {
    assertThrows(IllegalArgumentException.class, () -> service.actualizarPerfil(datos("ana", "A", "ana@empresa.com")));
  }
}
