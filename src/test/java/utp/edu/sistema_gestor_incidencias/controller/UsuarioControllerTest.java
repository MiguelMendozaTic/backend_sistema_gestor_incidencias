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

import utp.edu.sistema_gestor_incidencias.dto.role.RoleDTO;
import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.mappers.UsuarioMapper;
import utp.edu.sistema_gestor_incidencias.model.*;
import utp.edu.sistema_gestor_incidencias.security.SpringSecurityConfig;
import utp.edu.sistema_gestor_incidencias.security.TokenJwtConfig;
import utp.edu.sistema_gestor_incidencias.service.IncidenciaService;
import utp.edu.sistema_gestor_incidencias.service.UsuarioService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UsuarioController.class)
@Import(SpringSecurityConfig.class)
class UsuarioControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private UsuarioService usuarioService;

  @MockitoBean
  private IncidenciaService incidenciaService;

  @MockitoBean
  private UsuarioMapper usuarioMapper;
  @MockitoBean
  private TokenJwtConfig tokenJwtConfig;

  private Usuario usuarioEjemplo() {
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "EMPLEADO"));
    return new Usuario(1L, "abel", "123456", "Abel Torres", "abel@utp.edu", Estado.ACTIVO, Area.SISTEMAS,
        role);
  }

  // Jaime — PUT /api/usuario/{id}
  // @WithMockUser(username = "admin", roles = {"ADMIN"})
  @Test
  void modificarUsuario_retorna200YUsuarioModificado() throws Exception {
    Usuario usuario = usuarioEjemplo();
    usuario.setNombre("Abel Modificado");
    when(usuarioService.modificarUsuario(eq(1L), any(Usuario.class))).thenReturn(usuario);

    mockMvc.perform(put("/api/usuario/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuario))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Abel Modificado"));
  }

  // Jaime — PUT /api/usuario/{id}
  @Test
  void modificarUsuario_retorna404NotFound() throws Exception {
    Usuario usuario = usuarioEjemplo();
    usuario.setNombre("Abel Modificado");
    usuario.setId(99L);
    when(usuarioService.modificarUsuario(eq(99L), any(Usuario.class))).thenReturn(null);

    mockMvc.perform(put("/api/usuario/99")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuario))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Usuario no encontrado con id: 99"));
    ;
  }

  // Jaime — GET /api/usuario
  @Test
  void listarUsuarios_retorna200YListaDeUsuarios() throws Exception {
    when(usuarioService.listarUsuarios()).thenReturn(List.of(usuarioEjemplo()));

    mockMvc.perform(get("/api/usuario")
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].nombre").value("Abel Torres"));
  }

  // Jaime — GET /api/usuario
  @Test
  void listarUsuarios_retorna403Forbidden() throws Exception {
    when(usuarioService.listarUsuarios()).thenReturn(List.of(usuarioEjemplo()));

    mockMvc.perform(get("/api/usuario")
        .with(user("usuario").roles("EMPLEADO"))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  // Jaime — GET /api/usuario/paginado
  @Test
  void listarUsuariosPaginados_retorna200YListaDeUsuarios() throws Exception {
    Page<Usuario> pagina = new PageImpl<>(List.of(usuarioEjemplo()));
    when(usuarioService.listarPorNombreDescendente(any(Pageable.class))).thenReturn(pagina);

    mockMvc.perform(get("/api/usuario/paginado?page=0&size=2")
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.totalElements").value(1))
        .andExpect(jsonPath("$.page.totalPages").value(1))
        .andExpect(jsonPath("$.content[0].nombre").value("Abel Torres"));
  }

  // Jaime — GET /api/usuario/paginado
  // Cuando la parametro page es erroneo
  @Test
  void listarUsuariosPaginados_retorna400PageErroneo() throws Exception {
    Page<Usuario> pagina = new PageImpl<>(List.of(usuarioEjemplo()));
    when(usuarioService.listarPorNombreDescendente(any(Pageable.class))).thenReturn(pagina);

    mockMvc.perform(get("/api/usuario/paginado?page=-1&size=2")
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("El número de página no puede ser menor a 0"));
  }

  // Jaime — GET /api/usuario/tecnicos
  @Test
  void tecnicosDisponibles_retorna200YListaTecnicos() throws Exception {

    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "ROLE_TECNICO_NIVEL_1"));

    Usuario tecnico = new Usuario(
        2L,
        "johan",
        "123456",
        "Johan Gonzales",
        "johan@utp.edu",
        Estado.ACTIVO,
        Area.SISTEMAS,
        role);

    when(usuarioService.encontrarTecnicos("ROLE_TECNICO_NIVEL_1")).thenReturn(List.of(tecnico));

    mockMvc.perform(get("/api/usuario/tecnicos_disponibles")
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Busqueda eficiente"))
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.dato.length()").value(1))
        .andExpect(jsonPath("$.dato[0].nombre")
            .value("Johan Gonzales"));
  }

  // Jaime — GET /api/usuario/tecnicos
  @Test
  void tecnicosDisponibles_retorna403Forbidden() throws Exception {

    mockMvc.perform(get("/api/usuario/tecnicos")
        .with(user("usuarioComun").roles("EMPLEADO"))
        .with(csrf()))

        .andExpect(status().isForbidden());
  }

  // Jaime — GET /api/usuario/{id}
  @Test
  void obtenerUsuario_retorna200CuandoExiste() throws Exception {
    when(usuarioService.obtenerUsuario(1L)).thenReturn(Optional.of(usuarioEjemplo()));

    mockMvc.perform(get("/api/usuario/1")
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L));
  }

  // Jaime — GET /api/usuario/{id}
  @Test
  void obtenerUsuario_retorna404CuandoNoExiste() throws Exception {
    when(usuarioService.obtenerUsuario(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/usuario/99")
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isNotFound());
  }

  // Jaime — GET /api/usuario/{id}
  @Test
  void obtenerUsuario_retorna403CuandoNoAutorizado() throws Exception {
    when(usuarioService.obtenerUsuario(1L)).thenReturn(Optional.of(usuarioEjemplo()));

    mockMvc.perform(get("/api/usuario/1")
        .with(user("usuarioComun").roles("EMPLEADO"))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  // Jaime — POST /api/usuario/{id}/role
  @Test
  void updateRole_retorna200() throws Exception {
    RoleDTO roleDTO = new RoleDTO();
    roleDTO.setRole("TECNICO_NIVEL_1");

    Usuario usuario = usuarioEjemplo();

    when(usuarioService.actualizarRole(1L, roleDTO.getRole())).thenReturn(usuario);

    mockMvc.perform(post("/api/usuario/1/role")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(roleDTO))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isOk());
  }

  // Jaime — POST /api/usuario/{id}/role
  @Test
  void updateRole_retorna403() throws Exception {
    RoleDTO roleDTO = new RoleDTO();
    roleDTO.setRole("TECNICO_NIVEL_1");

    Usuario usuario = usuarioEjemplo();

    when(usuarioService.actualizarRole(1L, roleDTO.getRole())).thenReturn(usuario);

    mockMvc.perform(post("/api/usuario/1/role")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(roleDTO))
        .with(user("usuarioComun").roles("TECNICO"))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }
}