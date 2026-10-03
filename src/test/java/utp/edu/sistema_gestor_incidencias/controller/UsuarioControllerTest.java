package utp.edu.sistema_gestor_incidencias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import utp.edu.sistema_gestor_incidencias.dto.usuario.UsuarioDTO;
import utp.edu.sistema_gestor_incidencias.dto.usuario.UsuarioResponseDto;
import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.mappers.UsuarioMapper;
import utp.edu.sistema_gestor_incidencias.model.*;
import utp.edu.sistema_gestor_incidencias.security.SpringSecurityConfig;
import utp.edu.sistema_gestor_incidencias.security.TokenJwtConfig;
import utp.edu.sistema_gestor_incidencias.service.IncidenciaService;
import utp.edu.sistema_gestor_incidencias.service.UsuarioService;
import utp.edu.sistema_gestor_incidencias.service.auth.AuthService;

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
  @MockitoBean
  private AuthService authService;

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

  // GET /api/usuario/{username}/username — cada uno solo ve su propio perfil
  @Test
  void perfilPorUsername_propio_retorna200SinPasswordHash() throws Exception {
    when(usuarioService.obtenerUsuarioPorUsername("abel")).thenReturn(Optional.of(usuarioEjemplo()));

    mockMvc.perform(get("/api/usuario/abel/username")
        .with(user("abel").roles("EMPLEADO")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("abel"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @Test
  void perfilPorUsername_deOtroUsuario_retorna403() throws Exception {
    mockMvc.perform(get("/api/usuario/abel/username")
        .with(user("intruso").roles("EMPLEADO")))
        .andExpect(status().isForbidden());
  }

  @Test
  void perfilPorUsername_sinSesion_retorna401o403() throws Exception {
    mockMvc.perform(get("/api/usuario/abel/username"))
        .andExpect(result -> {
          int s = result.getResponse().getStatus();
          if (s != 401 && s != 403) throw new AssertionError("Esperaba 401/403 y fue " + s);
        });
  }

  // JSON tal como lo envía el formulario de alta del panel admin (user-form-component)
  private String jsonAltaDesdePanel(String nombre, String rol) {
    return jsonAltaDesdePanel(nombre, rol, "@Admin123");
  }

  private String jsonAltaDesdePanel(String nombre, String rol, String password) {
    return """
        {"area":"SISTEMAS","nombre":"%s","correo":"ana.torres@empresa.com","username":"ana.torres",
         "password":"%s","confirmPassword":"%s","rol":"%s"}
        """.formatted(nombre, password, password, rol);
  }

  // POST /api/usuario — contraseñas que no cumplen la política (6+, mayúscula, minúscula, número)
  @ParameterizedTest
  @ValueSource(strings = { "Ab1", "clave123", "CLAVE123", "ClaveSegura", "Clave123", "@Clave 123" })
  void crearUsuario_passwordDebil_retorna400(String password) throws Exception {
    mockMvc.perform(post("/api/usuario")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonAltaDesdePanel("Ana Torres", "ROLE_EMPLEADO", password))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password")
            .value("La contraseña debe tener mínimo 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial"));
  }

  // POST /api/usuario — alta por el admin con un nombre completo de más de 20 caracteres
  @Test
  void crearUsuario_admin_retorna201ConNombreCompleto() throws Exception {
    Usuario usuario = usuarioEjemplo();
    when(usuarioMapper.toEntity(any())).thenReturn(usuario);
    when(authService.register(any(Usuario.class), eq("ROLE_TECNICO_NIVEL_2"))).thenReturn(usuario);

    mockMvc.perform(post("/api/usuario")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonAltaDesdePanel("Ana Lucía Torres Gutiérrez", "ROLE_TECNICO_NIVEL_2"))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true));
  }

  // POST /api/usuario — solo el admin puede dar de alta usuarios
  @Test
  void crearUsuario_noAdmin_retorna403() throws Exception {
    mockMvc.perform(post("/api/usuario")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonAltaDesdePanel("Ana Torres", "ROLE_ADMIN"))
        .with(user("usuarioComun").roles("EMPLEADO"))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  // POST /api/usuario — nombre por encima del máximo permitido (35)
  @Test
  void crearUsuario_nombreMuyLargo_retorna400() throws Exception {
    mockMvc.perform(post("/api/usuario")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonAltaDesdePanel("A".repeat(36), "ROLE_EMPLEADO"))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.nombre").value("El nombre debe tener entre 3 y 35 caracteres"));
  }

  /* ---- Validaciones del alta (antes en /api/auth/register, ahora solo el admin) ---- */

  private UsuarioDTO userDtoEjemplo() {
    return new UsuarioDTO("jaime", "@Admin123", "Jaime Suarez", "jaimito@gmail.com", "ROLE_EMPLEADO",Area.CONTABILIDAD);
  }

  // Jaime — POST /api/usuario (alta por el admin)
  @Test
  void crearUsuario_retorna201YUsuarioCreado() throws Exception {

    Usuario user = new Usuario();
    user.setId(1L);
    user.setUsername(this.userDtoEjemplo().getUsername());

    UsuarioResponseDto userResponseDto = new UsuarioResponseDto();
    userResponseDto.setUsername(user.getUsername());
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "ROLE_EMPLEADO"));
    userResponseDto.setRoles(role);

    when(usuarioMapper.toEntity(any(UsuarioDTO.class))).thenReturn(user);
    when(authService.register(any(Usuario.class), any(String.class))).thenReturn(user);
    when(usuarioMapper.toResponseDto(any(Usuario.class))).thenReturn(userResponseDto);

    mockMvc.perform(post("/api/usuario")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userDtoEjemplo())) // el objeto que se envia al API
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Usuario Creado con exito!"))
        .andExpect(jsonPath("$.dato.username").value("jaime"))
        .andExpect(jsonPath("$.dato.roles[0].name").value("ROLE_EMPLEADO"));
  }

  // Jaime — POST /api/usuario (alta por el admin)
  @Test
  void crearUsuario_retorna400BadRequestNombreInvalido() throws Exception {

    UsuarioDTO usuarioDtoRequest = this.userDtoEjemplo();
    usuarioDtoRequest.setNombre("ja");

    Usuario user = new Usuario();
    user.setId(1L);
    user.setUsername(this.userDtoEjemplo().getUsername());

    UsuarioResponseDto userResponseDto = new UsuarioResponseDto();
    userResponseDto.setUsername(user.getUsername());
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "ROLE_EMPLEADO"));
    userResponseDto.setRoles(role);

    when(usuarioMapper.toEntity(any(UsuarioDTO.class))).thenReturn(user);
    when(authService.register(any(Usuario.class), any(String.class))).thenReturn(user);
    when(usuarioMapper.toResponseDto(any(Usuario.class))).thenReturn(userResponseDto);

    mockMvc.perform(post("/api/usuario")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioDtoRequest))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.errors.nombre").value("El nombre debe tener entre 3 y 35 caracteres"));
  }

  // Jaime — POST /api/usuario (alta por el admin)
  @Test
  void crearUsuario_retorna400BadRequestUsernameErroneo() throws Exception {

    UsuarioDTO usuarioDtoRequest = this.userDtoEjemplo();
    usuarioDtoRequest.setUsername("ch");

    Usuario user = new Usuario();
    user.setId(1L);
    user.setUsername(this.userDtoEjemplo().getUsername());

    UsuarioResponseDto userResponseDto = new UsuarioResponseDto();
    userResponseDto.setUsername(user.getUsername());
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "ROLE_EMPLEADO"));
    userResponseDto.setRoles(role);

    when(usuarioMapper.toEntity(any(UsuarioDTO.class))).thenReturn(user);
    when(authService.register(any(Usuario.class), any(String.class))).thenReturn(user);
    when(usuarioMapper.toResponseDto(any(Usuario.class))).thenReturn(userResponseDto);

    mockMvc.perform(post("/api/usuario")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioDtoRequest))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.errors.username").value("El username debe tener entre 3 y 20 caracteres"));
  }

  // Jaime — POST /api/usuario (alta por el admin)
  @Test
  void crearUsuario_retorna400BadRequestCorreoYPassword() throws Exception {

    UsuarioDTO usuarioDtoRequest = this.userDtoEjemplo();
    usuarioDtoRequest.setCorreo("jaimito.com.pe");
    usuarioDtoRequest.setPassword("1234");

    Usuario user = new Usuario();
    user.setId(1L);
    user.setUsername(this.userDtoEjemplo().getUsername());

    UsuarioResponseDto userResponseDto = new UsuarioResponseDto();
    userResponseDto.setUsername(user.getUsername());
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "ROLE_EMPLEADO"));
    userResponseDto.setRoles(role);

    when(usuarioMapper.toEntity(any(UsuarioDTO.class))).thenReturn(user);
    when(authService.register(any(Usuario.class), any(String.class))).thenReturn(user);
    when(usuarioMapper.toResponseDto(any(Usuario.class))).thenReturn(userResponseDto);

    mockMvc.perform(post("/api/usuario")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioDtoRequest))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.errors.correo").value("El formato del correo electrónico no es válido"))
        .andExpect(jsonPath("$.errors.password").value("La contraseña debe tener mínimo 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial"));
  }

  // Jaime — POST /api/usuario (alta por el admin)
  @Test
  void crearUsuario_retorna400BadRequestCorreoExiste() throws Exception {

    UsuarioDTO usuarioDtoRequest = this.userDtoEjemplo();
    usuarioDtoRequest.setCorreo("jaimito@gmail.com");

    Usuario user = new Usuario();
    user.setId(1L);
    user.setUsername(this.userDtoEjemplo().getUsername());

    UsuarioResponseDto userResponseDto = new UsuarioResponseDto();
    userResponseDto.setUsername(user.getUsername());
    Set<Role> role = new HashSet<>();
    role.add(new Role(1L, "ROLE_EMPLEADO"));
    userResponseDto.setRoles(role);

    when(usuarioMapper.toEntity(any(UsuarioDTO.class))).thenReturn(user);
    when(authService.register(any(Usuario.class), any(String.class)))
        .thenThrow(new IllegalArgumentException("El correo electrónico ya se encuentra registrado."));

    mockMvc.perform(post("/api/usuario")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioDtoRequest))
        .with(user("adminUser").roles("ADMIN"))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("El correo electrónico ya se encuentra registrado."));
  }
}
