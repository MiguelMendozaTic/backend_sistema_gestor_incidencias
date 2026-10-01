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

import utp.edu.sistema_gestor_incidencias.dto.incidencia.IncidenciaDTO;
import utp.edu.sistema_gestor_incidencias.dto.incidencia.IncidenciaResponseDTO;
import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;
import utp.edu.sistema_gestor_incidencias.exception.UsuarioNoEncontradoException;
import utp.edu.sistema_gestor_incidencias.mappers.IncidenciaMapper;
import utp.edu.sistema_gestor_incidencias.model.*;
import utp.edu.sistema_gestor_incidencias.security.SpringSecurityConfig;
import utp.edu.sistema_gestor_incidencias.security.TokenJwtConfig;
import utp.edu.sistema_gestor_incidencias.service.IncidenciaService;
import utp.edu.sistema_gestor_incidencias.service.UsuarioService;

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

@WebMvcTest(IncidenciaController.class)
@Import(SpringSecurityConfig.class)
class IncidenteControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private IncidenciaMapper incidenciaMapper;

  @MockitoBean
  private IncidenciaService incidenteService;

  @MockitoBean
  private UsuarioService usuarioService;

  @MockitoBean
  private AuthenticationManager authenticationManager;

  @MockitoBean
  private UserDetailsService userDetailsService;

  @MockitoBean
  private TokenJwtConfig tokenJwtConfig;

  private Usuario usuarioEjemplo() {
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "Empleado"));

    return new Usuario(1L, "jaime", "123456", "Jaime Ruiz", "jaime@utp.edu", Estado.ACTIVO,
        Area.CONTABILIDAD,
        role);
  }

  private Usuario tecnicoEjemplo() {
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "TECNICO_NIVEL_1"));
    return new Usuario(2L, "johan", "123456", "Johan Gonzales", "johan@utp.edu", Estado.ACTIVO,
        Area.SISTEMAS,
        role);
  }

  private Incidencia incidenciaEjemplo() {
    return new Incidencia(1L, "PC no enciende", "El equipo no responde al inicio", EstadoIncidencia.ABIERTO,
        usuarioEjemplo(), new Date(),tecnicoEjemplo());
  }

  // Jaime — POST /api/incidencia
  @Test
  void crearIncidencia_retorna201YIncidenciaCreada() throws Exception {
    Incidencia incidencia = incidenciaEjemplo();
    IncidenciaResponseDTO response = new IncidenciaResponseDTO();
    response.setId(incidencia.getId());
    response.setTitulo(incidencia.getTitulo());
    response.setEstado(incidencia.getEstado());

    when(usuarioService.obtenerUsuario(1L)).thenReturn(Optional.of(usuarioEjemplo()));
    when(usuarioService.obtenerUsuario(2L)).thenReturn(Optional.of(tecnicoEjemplo()));
    when(incidenteService.crearIncidencia(any(IncidenciaDTO.class))).thenReturn(incidencia);
    when(incidenciaMapper.toResponseDto(any(Incidencia.class))).thenReturn(response);

    mockMvc.perform(post("/api/incidencia")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(incidencia))
        .with(user("empleado").roles("EMPLEADO"))
        .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.titulo").value("PC no enciende"))
        .andExpect(jsonPath("$.estado").value("ABIERTO"));
  }

  // Jaime — POST /api/incidencia
  @Test
  void crearIncidencia_retorna404NotFound() throws Exception {
    Incidencia incidencia = incidenciaEjemplo();
    incidencia.getUsuario().setId(99L);
    ;
    when(usuarioService.obtenerUsuario(99L)).thenReturn(Optional.empty());
    when(usuarioService.obtenerUsuario(2L)).thenReturn(Optional.of(tecnicoEjemplo()));
    when(incidenteService.crearIncidencia(any(IncidenciaDTO.class)))
        .thenThrow(new UsuarioNoEncontradoException("El usuario no encontrado"));

    mockMvc.perform(post("/api/incidencia")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(incidencia))
        .with(user("empleado").roles("EMPLEADO"))
        .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string("El usuario no encontrado"));

  }

  // Jaime — PUT /api/incidencia/{id}
  @Test
  void modificarIncidencia_retorna200YIncidenciaModificada() throws Exception {
    Incidencia incidencia = incidenciaEjemplo();
    incidencia.setEstado(EstadoIncidencia.PENDIENTE);
    when(usuarioService.obtenerUsuario(1L)).thenReturn(Optional.of(usuarioEjemplo()));
    when(usuarioService.obtenerUsuario(2L)).thenReturn(Optional.of(tecnicoEjemplo()));
    when(incidenteService.modificarIncidencia(eq(1L), any(Incidencia.class))).thenReturn(incidencia);

    mockMvc.perform(put("/api/incidencia/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(incidencia))
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estado").value("PENDIENTE"));
  }

  // Jaime — put /api/incidencia/{id}
  @Test
  void modificarIncidencia_retorna404NotFound() throws Exception {
    Incidencia incidencia = incidenciaEjemplo();
    incidencia.setDescripcion("El equipo prende, pero demora como 5 minutos en iniciar sesión");
    incidencia.getTecnico().setId(99L);
    ;
    when(usuarioService.obtenerUsuario(99L)).thenReturn(Optional.empty());

    mockMvc.perform(put("/api/incidencia/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(incidencia))
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Usuario no encontrado con ese ID"));

  }

  // Jaime — GET /api/incidencia
  @Test
  void listarIncidencias_retorna200YListaDeIncidencias() throws Exception {
    when(incidenteService.listarIncidencias()).thenReturn(List.of(incidenciaEjemplo()));

    mockMvc.perform(get("/api/incidencia")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].titulo").value("PC no enciende"));
  }

  // Jaime — GET /api/incidencia
  @Test
  void listarIncidencias_retorna403Forbiden() throws Exception {
    when(incidenteService.listarIncidencias()).thenReturn(List.of(incidenciaEjemplo()));
    mockMvc.perform(get("/api/incidencia")
        .with(user("empleado").roles("EMPLEADO"))
        .with(csrf()))

        .andExpect(status().isForbidden());

  }

  // Jaime — GET /api/incidencia/misIncidencias
  @Test
  void misIncidencias_retorna200YListaIncidenciasDelUsuario() throws Exception {

    Incidencia incidencia = incidenciaEjemplo();

    IncidenciaResponseDTO response = new IncidenciaResponseDTO();
    response.setId(incidencia.getId());
    response.setTitulo(incidencia.getTitulo());
    response.setEstado(incidencia.getEstado());

    when(incidenteService.misIncidencias()).thenReturn(List.of(incidencia));

    when(incidenciaMapper.toResponseDto(any(Incidencia.class)))
        .thenReturn(response);

    mockMvc.perform(get("/api/incidencia/misIncidencias")
        .with(user("empleado").roles("EMPLEADO"))
        .with(csrf()))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].titulo").value("PC no enciende"));
  }

  // Jaime — GET /api/incidencia/misIncidencias
  @Test
  void misIncidencias_retorna403Forbidden() throws Exception {

    mockMvc.perform(get("/api/incidencia/misIncidencias")
        .with(user("usuario").roles("INVITADO"))
        .with(csrf()))

        .andExpect(status().isForbidden());
  }

  // Jaime — GET /api/incidencia/paginado
  @Test
  void listarIncidenciasPaginado_retorna200YPaginaCorrecta() throws Exception {

    Incidencia incidencia = incidenciaEjemplo();

    Page<Incidencia> pagina = new PageImpl<>(List.of(incidencia));

    when(incidenteService.listarIncidenciasPaginado(any(Pageable.class)))
        .thenReturn(pagina);

    mockMvc.perform(get("/api/incidencia/paginado")
        .param("page", "0")
        .param("size", "5")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].titulo")
            .value("PC no enciende"));
  }

  // Jaime — GET /api/incidencia/paginado
  /*@Test
  void listarIncidenciasPaginado_retorna403Forbidden() throws Exception {

    mockMvc.perform(get("/api/incidencia/paginado")
        .param("page", "0")
        .param("size", "5")
        .with(user("empleado").roles("EMPLEADO"))
        .with(csrf()))

        .andExpect(status().isForbidden());
  }*/

  // Johan — GET /api/incidencia/{id}
  @Test
  void obtenerIncidencia_retorna200CuandoExiste() throws Exception {
    when(incidenteService.obtenerIncidencia(1L)).thenReturn(Optional.of(incidenciaEjemplo()));

    mockMvc.perform(get("/api/incidencia/1")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L));
  }

  @Test
  void obtenerIncidencia_retorna404CuandoNoExiste() throws Exception {
    when(incidenteService.obtenerIncidencia(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/incidencia/99")
        .with(user("admin").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Incidencia no encontrada con id: 99"));
    ;
  }

}
