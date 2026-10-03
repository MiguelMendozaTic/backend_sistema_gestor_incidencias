package utp.edu.sistema_gestor_incidencias.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import utp.edu.sistema_gestor_incidencias.security.SpringSecurityConfig;
import utp.edu.sistema_gestor_incidencias.security.TokenJwtConfig;
import utp.edu.sistema_gestor_incidencias.service.auth.AuthService;

// Sin registro público: /api/auth solo expone las comprobaciones de disponibilidad para el admin
@WebMvcTest(AuthController.class)
@Import(SpringSecurityConfig.class)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;
  @MockitoBean
  private TokenJwtConfig tokenJwtConfig;

  private static ResultMatcher denegado() {
    return result -> {
      int s = result.getResponse().getStatus();
      if (s != 401 && s != 403) throw new AssertionError("Esperaba 401/403 y fue " + s);
    };
  }

  @Test
  void existeUsername_admin_retorna200() throws Exception {
    when(authService.existsByUsername("jaime")).thenReturn(true);

    mockMvc.perform(get("/api/auth/jaime").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.exists").value(true));
  }

  @Test
  void existeCorreo_admin_retorna200() throws Exception {
    when(authService.existsByCorreo("jaimito@gmail.com")).thenReturn(false);

    mockMvc.perform(get("/api/auth/jaimito@gmail.com/validacion").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.exists").value(false));
  }

  // Evita que cualquiera averigüe qué usuarios/correos existen
  @Test
  void existeUsername_sinSesion_denegado() throws Exception {
    mockMvc.perform(get("/api/auth/jaime")).andExpect(denegado());
  }

  @Test
  void existeUsername_empleado_retorna403() throws Exception {
    mockMvc.perform(get("/api/auth/jaime").with(user("emp").roles("EMPLEADO")))
        .andExpect(status().isForbidden());
  }

  // El registro público ya no existe
  @Test
  void registroPublico_yaNoEstaDisponible() throws Exception {
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"nuevo\",\"password\":\"@Admin123\"}")
        .with(csrf()))
        .andExpect(denegado());
  }
}
