package utp.edu.sistema_gestor_incidencias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import utp.edu.sistema_gestor_incidencias.dto.seguimiento.SeguimientoDTO;
import utp.edu.sistema_gestor_incidencias.dto.seguimiento.SeguimientoResponseDto;
import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;
import utp.edu.sistema_gestor_incidencias.exception.IncidenciaNotFoundException;
import utp.edu.sistema_gestor_incidencias.exception.UsuarioNoEncontradoException;
import utp.edu.sistema_gestor_incidencias.mappers.SeguimientoMapper;
import utp.edu.sistema_gestor_incidencias.model.*;
import utp.edu.sistema_gestor_incidencias.security.SpringSecurityConfig;
import utp.edu.sistema_gestor_incidencias.security.TokenJwtConfig;
import utp.edu.sistema_gestor_incidencias.service.IncidenciaService;
import utp.edu.sistema_gestor_incidencias.service.SeguimientoService;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(SeguimientoController.class)
@Import(SpringSecurityConfig.class)
class SeguimientoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private SeguimientoService seguimientoService;

  @MockitoBean
  private IncidenciaService incidenciaService;

  @MockitoBean
  private SeguimientoMapper seguimientoMapper;

  @MockitoBean
  private AuthenticationManager authenticationManager;

  @MockitoBean
  private UserDetailsService userDetailsService;

  @MockitoBean
  private TokenJwtConfig tokenJwtConfig;

  private Seguimiento seguimientoEjemplo() {
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "TECNICO_NIVEL_2"));
    Usuario usuario = new Usuario(1L, "stephani", "123456", "Stephani Lopez", "stephani@utp.edu",
        Estado.ACTIVO, Area.SISTEMAS,
        role);
    Incidencia incidencia = new Incidencia(1L, "PC no enciende", "Descripcion",
        EstadoIncidencia.ABIERTO, usuario,  new Date(),usuario);
    return new Seguimiento(1L, incidencia, "Revisando el equipo", new Date(), Estado.ACTIVO, usuario);
  }

  // Stephani — POST /api/seguimiento
  @Test
  void crearSeguimiento_retorna201YSeguimientoCreado() throws Exception {

    SeguimientoDTO dto = new SeguimientoDTO();
    dto.setIdIncidencia(1L);
    dto.setEstado(Estado.ACTIVO);
    dto.setComentario("Revisando el equipo");

    Seguimiento seguimiento = seguimientoEjemplo();

    SeguimientoResponseDto responseDto = new SeguimientoResponseDto();
    responseDto.setId(1L);
    responseDto.setComentario("Revisando el equipo");

    when(incidenciaService.obtenerIncidencia(1L)).thenReturn(Optional.of(seguimiento.getIncidencia()));
    when(seguimientoService.crearSeguimiento(any(Seguimiento.class))).thenReturn(seguimiento);
    when(seguimientoMapper.toEntity(any(SeguimientoDTO.class)))
        .thenReturn(seguimiento);
    when(seguimientoMapper.toResponseDto(any(Seguimiento.class)))
        .thenReturn(responseDto);

    mockMvc.perform(post("/api/seguimiento")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto))
        .with(user("usuario").roles("EMPLEADO"))
        .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.comentario").value("Revisando el equipo"));
  }

  // JAIME — POST /api/seguimiento
  @Test
  void crearSeguimiento_retorna404UsuarioNoEncontrado() throws Exception {

    SeguimientoDTO dto = new SeguimientoDTO();
    dto.setIdIncidencia(1L);
    dto.setEstado(Estado.ACTIVO);
    dto.setComentario("Revisando el equipo");

    Seguimiento seguimiento = seguimientoEjemplo();

    SeguimientoResponseDto responseDto = new SeguimientoResponseDto();
    responseDto.setId(1L);
    responseDto.setComentario("Revisando el equipo");
    when(seguimientoMapper.toEntity(any(SeguimientoDTO.class)))
        .thenReturn(seguimiento);
    when(seguimientoService.crearSeguimiento(any(Seguimiento.class)))
        .thenThrow(new UsuarioNoEncontradoException("El usuario no se ha encontrado"));
    when(incidenciaService.obtenerIncidencia(1L)).thenReturn(Optional.of(seguimiento.getIncidencia()));

    mockMvc.perform(post("/api/seguimiento")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto))
        .with(user("usuario").roles("EMPLEADO"))
        .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string("El usuario no se ha encontrado"));

  }

  @Test
  void modificarSeguimiento_retorna200YSeguimientoModificado() throws Exception {
    Seguimiento seguimiento = seguimientoEjemplo();
    seguimiento.setComentario("Equipo reparado");
    when(incidenciaService.obtenerIncidencia(1L)).thenReturn(Optional.of(seguimiento.getIncidencia()));
    when(seguimientoService.modificarSeguimiento(eq(1L), any(Seguimiento.class))).thenReturn(seguimiento);

    mockMvc.perform(put("/api/seguimiento/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(seguimiento))
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comentario").value("Equipo reparado"));
  }

  // Jaime — PUT /api/seguimiento/{id}
  @Test
  void modificarSeguimiento_retorna403Forbiden() throws Exception {
    Seguimiento seguimiento = seguimientoEjemplo();
    seguimiento.setComentario("Equipo reparado");
    when(incidenciaService.obtenerIncidencia(1L)).thenReturn(Optional.of(seguimiento.getIncidencia()));
    when(seguimientoService.modificarSeguimiento(eq(1L), any(Seguimiento.class))).thenReturn(seguimiento);

    mockMvc.perform(put("/api/seguimiento/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(seguimiento))
        .with(user("jacinto").roles("EMPLEADO"))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  // jaime — GET /api/seguimiento
  @Test
  void listarSeguimientos_retorna200YListaDeSeguimientos() throws Exception {
    when(seguimientoService.listarSeguimientos()).thenReturn(List.of(seguimientoEjemplo()));

    mockMvc.perform(get("/api/seguimiento")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].comentario").value("Revisando el equipo"));
  }

  // jaime — GET /api/seguimiento
  @Test
  void listarSeguimientos_retorna403Forbiden() throws Exception {
    when(seguimientoService.listarSeguimientos()).thenReturn(List.of(seguimientoEjemplo()));

    mockMvc.perform(get("/api/seguimiento")
        .with(user("jacinto").roles("EMPLEADO"))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  // JAIME — GET /api/seguimiento/paginado
  @Test
  void seguimientoPaginado_retorna200YPaginaCorrecta() throws Exception {

    Seguimiento seguimiento = seguimientoEjemplo();

    Page<Seguimiento> pagina = new PageImpl<>(List.of(seguimiento));

    when(seguimientoService.listarSeguimientosPaginado(any(Pageable.class)))
        .thenReturn(pagina);

    mockMvc.perform(get("/api/seguimiento/paginado")
        .param("page", "0")
        .param("size", "5")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].comentario")
            .value("Revisando el equipo"));
  }

  // Stephani — GET /api/seguimiento/paginado
  @Test
  void seguimientoPaginado_retorna403Forbidden() throws Exception {

    mockMvc.perform(get("/api/seguimiento/paginado")
        .param("page", "0")
        .param("size", "5")
        .with(user("jacinto").roles("EMPLEADO"))
        .with(csrf()))

        .andExpect(status().isForbidden());
  }

  // Jamil — GET /api/seguimiento/{id}
  @Test
  void obtenerSeguimiento_retorna200CuandoExiste() throws Exception {
    when(seguimientoService.obtenerSeguimiento(1L)).thenReturn(Optional.of(seguimientoEjemplo()));

    mockMvc.perform(get("/api/seguimiento/1")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.idSeguimiento").value(1L));
  }

  @Test
  void obtenerSeguimiento_retorna404CuandoNoExiste() throws Exception {
    when(seguimientoService.obtenerSeguimiento(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/seguimiento/99")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isNotFound());
  }

  // Jaime — GET /api/seguimiento/{id}/seguimientos
  @Test
  void obtenerMisSeguimientos_retorna200YListaSeguimientos() throws Exception {

    Seguimiento seguimiento = seguimientoEjemplo();

    SeguimientoResponseDto responseDto = new SeguimientoResponseDto();
    responseDto.setId(1L);
    responseDto.setComentario("Revisando el equipo");

    when(seguimientoService.misSeguimientos(1L)).thenReturn(List.of(seguimiento));

    when(seguimientoMapper.toResponseDto(any(Seguimiento.class)))
        .thenReturn(responseDto);

    mockMvc.perform(get("/api/seguimiento/1/seguimientos")
        .with(user("empleado").roles("EMPLEADO"))
        .with(csrf()))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].comentario")
            .value("Revisando el equipo"));
  }

  // Jaime — GET /api/seguimiento/{id}/seguimientos
  @Test
  void obtenerMisSeguimientos_retorna403Forbidden() throws Exception {

    mockMvc.perform(get("/api/seguimiento/1/seguimientos")
        .with(user("jacinto").roles("NUEVO"))
        .with(csrf()))

        .andExpect(status().isForbidden());
  }

  // Seguimiento //
  @Test
  void listarSeguimientos_retornaListaVacia() throws Exception {
    when(seguimientoService.listarSeguimientos()).thenReturn(List.of());

    mockMvc.perform(get("/api/seguimiento")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void listarSeguimientos_retornaMultiplesSeguimientos() throws Exception {
    Seguimiento s1 = seguimientoEjemplo();
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "TECNICO_NIVEL_2"));
    Usuario usuario = new Usuario(2L, "jamil", "123456", "Jamil Alarcon", "jml@utp.edu", Estado.ACTIVO,
        Area.SISTEMAS,
        role);
    Incidencia incidencia = new Incidencia(2L, "Monitor falla", "Descripcion 2",
        EstadoIncidencia.ABIERTO, usuario, new Date() ,usuario);
    Seguimiento s2 = new Seguimiento(2L, incidencia, "Cambio de monitor", new Date(), Estado.ACTIVO,
        usuario);

    when(seguimientoService.listarSeguimientos()).thenReturn(List.of(s1, s2));

    mockMvc.perform(get("/api/seguimiento")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void obtenerSeguimiento_validaComentarioCuandoExiste() throws Exception {
    when(seguimientoService.obtenerSeguimiento(1L)).thenReturn(Optional.of(seguimientoEjemplo()));

    mockMvc.perform(get("/api/seguimiento/1")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.idSeguimiento").value(1L))
        .andExpect(jsonPath("$.comentario").value("Revisando el equipo"));
  }

  @Test
  void obtenerSeguimiento_idInvalido_retorna404() throws Exception {
    when(seguimientoService.obtenerSeguimiento(-1L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/seguimiento/-1")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  void modificarSeguimiento_cuandoIncidenciaNoExiste_retorna404() throws Exception {
    var seguimiento = seguimientoEjemplo();
    seguimiento.setComentario("Equipo reparado, puede recogerlo.");
    seguimiento.getIncidencia().setId(99L); // ID que no existe

    when(seguimientoService.modificarSeguimiento(eq(1L), any(Seguimiento.class)))
        .thenThrow(new IncidenciaNotFoundException("Incidencia no encontrado con ese ID"));

    mockMvc.perform(put("/api/seguimiento/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(seguimiento))
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Incidencia no encontrado con ese ID"));
  }
}
