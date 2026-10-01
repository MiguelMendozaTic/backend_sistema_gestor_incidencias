package utp.edu.sistema_gestor_incidencias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import utp.edu.sistema_gestor_incidencias.dto.usuario.UsuarioDTO;
import utp.edu.sistema_gestor_incidencias.dto.usuario.UsuarioResponseDto;
import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.mappers.UsuarioMapper;
import utp.edu.sistema_gestor_incidencias.model.Role;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.security.SpringSecurityConfig;
import utp.edu.sistema_gestor_incidencias.security.TokenJwtConfig;
import utp.edu.sistema_gestor_incidencias.service.auth.AuthService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AuthController.class)
@Import(SpringSecurityConfig.class)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private AuthService authService;
  @MockitoBean
  private UsuarioMapper usuarioMapper;
  @MockitoBean
  private TokenJwtConfig tokenJwtConfig;

  private UsuarioDTO userDtoEjemplo() {
    return new UsuarioDTO("jaime", "123456", "Jaime Suarez", "jaimito@gmail.com", "ROLE_EMPLEADO",Area.CONTABILIDAD);
  }

  // Jaime — POST /api/usuario
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

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userDtoEjemplo())) // el objeto que se envia al API
        .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Usuario Creado con exito!"))
        .andExpect(jsonPath("$.dato.username").value("jaime"))
        .andExpect(jsonPath("$.dato.roles[0].name").value("ROLE_EMPLEADO"));
  }

  // Jaime — POST /api/usuario
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

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioDtoRequest))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.errors.nombre").value("El nombre debe tener entre 3 y 35 caracteres"));
  }

  // Jaime — POST /api/usuario
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

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioDtoRequest))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.errors.username").value("El username debe tener entre 3 y 20 caracteres"));
  }

  // Jaime — POST /api/usuario
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

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioDtoRequest))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.errors.correo").value("El formato del correo electrónico no es válido"))
        .andExpect(jsonPath("$.errors.password").value("La contraseña debe tener al menos 5 caracteres"));
  }

  // Jaime — POST /api/usuario
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

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioDtoRequest))
        .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("El correo electrónico ya se encuentra registrado."));
  }
}
