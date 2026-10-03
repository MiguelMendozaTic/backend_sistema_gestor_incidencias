package utp.edu.sistema_gestor_incidencias.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import utp.edu.sistema_gestor_incidencias.dto.incidencia.EstadoIncidenciaRequest;
import utp.edu.sistema_gestor_incidencias.dto.incidencia.IncidenciaDTO;
import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;
import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.model.Role;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.EquipoRepository;
import utp.edu.sistema_gestor_incidencias.repository.IncidenciaRepository;

// Regla del solicitante al crear incidencias: solo el admin puede registrarla a nombre de otro usuario
class IncidenciaServiceTest {

  private IncidenciaRepository incidenciaRepository;
  private UsuarioService usuarioService;
  private IncidenciaService service;

  private final Usuario otroUsuario = usuario(9L, "otro", "ROLE_EMPLEADO");

  @BeforeEach
  void setUp() {
    incidenciaRepository = mock(IncidenciaRepository.class);
    usuarioService = mock(UsuarioService.class);
    service = new IncidenciaService(incidenciaRepository, usuarioService, mock(SeguimientoService.class),
        mock(EquipoRepository.class), mock(ApplicationEventPublisher.class));

    when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(inv -> inv.getArgument(0));
    when(usuarioService.encontrarTecnicos(any())).thenReturn(List.of());
    when(usuarioService.buscarPorId(9L)).thenReturn(Optional.of(otroUsuario));
  }

  private static Usuario usuario(Long id, String username, String rol) {
    Usuario u = new Usuario();
    u.setId(id);
    u.setUsername(username);
    u.setNombre(username);
    u.setArea(Area.SISTEMAS);
    u.setEstado(Estado.ACTIVO);
    u.setRoles(Set.of(new Role(1L, rol)));
    return u;
  }

  private static IncidenciaDTO dtoConSolicitante(Long solicitanteId) {
    IncidenciaDTO dto = new IncidenciaDTO();
    dto.setTitulo("No imprime");
    dto.setDescripcion("La impresora no responde");
    dto.setSolicitanteId(solicitanteId);
    return dto;
  }

  @Test
  void empleado_siempreEsElSolicitante_aunqueEnvieOtroId() {
    Usuario empleado = usuario(1L, "empleado", "ROLE_EMPLEADO");
    when(usuarioService.obtenerUsuarioSession()).thenReturn(Optional.of(empleado));

    Incidencia creada = service.crearIncidencia(dtoConSolicitante(9L));

    assertSame(empleado, creada.getSolicitante());
    verify(usuarioService, never()).buscarPorId(anyLong());
  }

  @Test
  void admin_puedeRegistrarAnombreDeOtroUsuario() {
    when(usuarioService.obtenerUsuarioSession()).thenReturn(Optional.of(usuario(2L, "admin", "ROLE_ADMIN")));

    Incidencia creada = service.crearIncidencia(dtoConSolicitante(9L));

    assertSame(otroUsuario, creada.getSolicitante());
  }

  @Test
  void admin_sinSolicitante_quedaComoSolicitante() {
    Usuario admin = usuario(2L, "admin", "ROLE_ADMIN");
    when(usuarioService.obtenerUsuarioSession()).thenReturn(Optional.of(admin));

    Incidencia creada = service.crearIncidencia(dtoConSolicitante(null));

    assertSame(admin, creada.getSolicitante());
  }

  /* ---- Acceso al detalle y al cambio de estado ---- */

  private Incidencia incidencia(Usuario registro, Usuario solicitante, Usuario tecnico) {
    Incidencia i = new Incidencia();
    i.setId(50L);
    i.setUsuario(registro);
    i.setSolicitante(solicitante);
    i.setTecnico(tecnico);
    i.setEstado(EstadoIncidencia.PENDIENTE);
    when(incidenciaRepository.findById(50L)).thenReturn(Optional.of(i));
    return i;
  }

  private final Usuario admin = usuario(2L, "admin", "ROLE_ADMIN");
  private final Usuario empleado = usuario(1L, "empleado", "ROLE_EMPLEADO");
  private final Usuario tecnico = usuario(3L, "tecnico", "ROLE_TECNICO_NIVEL_1");
  private final Usuario ajeno = usuario(4L, "ajeno", "ROLE_EMPLEADO");

  private void sesion(Usuario u) {
    when(usuarioService.obtenerUsuarioSession()).thenReturn(Optional.of(u));
  }

  @Test
  void detalle_solicitante_puedeVerLaIncidenciaQueElAdminRegistroAsuNombre() {
    Incidencia inc = incidencia(admin, empleado, null);
    sesion(empleado);
    assertSame(inc, service.obtenerIncidenciaVisible(50L));
  }

  @Test
  void detalle_tecnicoAsignado_puedeVer() {
    Incidencia inc = incidencia(empleado, empleado, tecnico);
    sesion(tecnico);
    assertSame(inc, service.obtenerIncidenciaVisible(50L));
  }

  @Test
  void detalle_usuarioAjeno_recibeAccesoDenegado() {
    incidencia(empleado, empleado, tecnico);
    sesion(ajeno);
    assertThrows(AccessDeniedException.class, () -> service.obtenerIncidenciaVisible(50L));
  }

  @Test
  void detalle_admin_veCualquierIncidencia() {
    Incidencia inc = incidencia(empleado, empleado, null);
    sesion(admin);
    assertSame(inc, service.obtenerIncidenciaVisible(50L));
  }

  @Test
  void estado_tecnicoNoAsignado_noPuedeCambiarlo() {
    Usuario otroTecnico = usuario(5L, "otroTec", "ROLE_TECNICO_NIVEL_2");
    incidencia(empleado, empleado, tecnico);
    sesion(otroTecnico);
    EstadoIncidenciaRequest req = new EstadoIncidenciaRequest(EstadoIncidencia.ABIERTO, 50L);

    assertThrows(AccessDeniedException.class, () -> service.modificarEstado(req));
    verify(incidenciaRepository, never()).save(any());
  }

  @Test
  void estado_tecnicoAsignado_puedeCambiarlo() {
    Incidencia inc = incidencia(empleado, empleado, tecnico);
    sesion(tecnico);
    EstadoIncidenciaRequest req = new EstadoIncidenciaRequest(EstadoIncidencia.ABIERTO, 50L);

    service.modificarEstado(req);

    assertSame(EstadoIncidencia.ABIERTO, inc.getEstado());
  }
}
