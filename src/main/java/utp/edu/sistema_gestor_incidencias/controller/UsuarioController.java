package utp.edu.sistema_gestor_incidencias.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import utp.edu.sistema_gestor_incidencias.dto.ApiResponse;
import utp.edu.sistema_gestor_incidencias.dto.role.RoleDTO;
import utp.edu.sistema_gestor_incidencias.dto.usuario.TecnicosDTO;
import utp.edu.sistema_gestor_incidencias.dto.usuario.UsuarioResponseDto;
import utp.edu.sistema_gestor_incidencias.mappers.UsuarioMapper;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.service.IncidenciaService;
import utp.edu.sistema_gestor_incidencias.service.UsuarioService;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

  private UsuarioService usuarioService;

  private IncidenciaService incidenciaService;

  private UsuarioMapper usuarioMapper;

  UsuarioController(UsuarioService usuarioService,IncidenciaService incidenciaService, UsuarioMapper usuarioMapper) {
    this.usuarioService = usuarioService;
    this.usuarioMapper = usuarioMapper;
    this.incidenciaService = incidenciaService;

  }

  @PutMapping("/{id}")
  public ResponseEntity<?> modificarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
    Usuario modificado = usuarioService.modificarUsuario(id, usuario);
    if (modificado != null)
      return ResponseEntity.ok(modificado);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado con id: " + id);
  }

  @GetMapping
  public ResponseEntity<List<Usuario>> listarUsuarios() {
    return ResponseEntity.ok(usuarioService.listarUsuarios());
  }

  @GetMapping("/paginado")
  public PagedModel<Usuario> listarUsuariosPaginados(
      @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "El número de página no puede ser menor a 0") int page,
      @RequestParam(value = "size", defaultValue = "5") @Min(value = 1, message = "El tamaño de página debe ser al menos 1") @Max(value = 100, message = "No puedes solicitar más de 100 registros por página") int size,
      @RequestParam(value = "texto", defaultValue = "") String texto) {
    Pageable pageable = PageRequest.of(page, size);
    if (texto.trim().length() >= 2) {
      Page<Usuario> usuarios = this.usuarioService.buscarPorNombreCorreoDesc(texto, pageable);
      return new PagedModel<>(usuarios);
    }
    Page<Usuario> usuarios = this.usuarioService.listarPorNombreDescendente(pageable);
    return new PagedModel<>(usuarios);
  }

  @GetMapping("/tecnicos_disponibles")
  public ResponseEntity<ApiResponse<List<Usuario>>> tecnicosDisponibles() {
    List<Usuario> tecnicos1 = usuarioService.encontrarTecnicos("ROLE_TECNICO_NIVEL_1");
    List<Usuario> tecnicos2 = usuarioService.encontrarTecnicos("ROLE_TECNICO_NIVEL_2");
    List<Usuario> tecnicos3 = usuarioService.encontrarTecnicos("ROLE_TECNICO_NIVEL_3");
    List<Usuario> listaFinal = Stream.concat(
        Stream.concat(tecnicos1.stream(), tecnicos2.stream()),
        tecnicos3.stream()).collect(Collectors.toList());

    ApiResponse<List<Usuario>> response = new ApiResponse<List<Usuario>>(
        true, "Busqueda eficiente", 200, listaFinal);

    return ResponseEntity.status(HttpStatus.OK.value()).body(response);
  }

  @GetMapping("/lista_tecnicos")
  public ResponseEntity<ApiResponse<List<TecnicosDTO>>> listaDeTecnicos() {
    List<TecnicosDTO> tecnicos = incidenciaService.listaTecnicoConIncidencia();

    ApiResponse<List<TecnicosDTO>> response = new ApiResponse<List<TecnicosDTO>>(
        true, "Busqueda eficiente", 200, tecnicos);

    return ResponseEntity.status(HttpStatus.OK.value()).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> obtenerUsuario(@PathVariable Long id) {
    var user = usuarioService.obtenerUsuario(id);
    if (user.isPresent())
      return ResponseEntity.ok(user.get());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado con id: " + id);
  }

  @GetMapping("/{username}/username")
  public ResponseEntity<?> obtenerUsuarioUsername(@PathVariable String username) {
    var user = usuarioService.obtenerUsuarioPorUsername(username);
    if (user.isPresent())
      return ResponseEntity.ok(user.get());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado con username: " + username);
  }

  // actualizar Rol
  @PostMapping("/{id}/role")
  public ResponseEntity<ApiResponse<UsuarioResponseDto>> updateRole(@PathVariable Long id, @RequestBody RoleDTO role) {
    Usuario user = usuarioService.actualizarRole(id, role.getRole());
    UsuarioResponseDto userResponseDto = usuarioMapper.toResponseDto(user);

    ApiResponse<UsuarioResponseDto> response = new ApiResponse<UsuarioResponseDto>(
        true, "Rol actualizado con exito!", 200, userResponseDto);

    return ResponseEntity.status(HttpStatus.OK.value()).body(response);
  }

  // eliminar Rol
  @PostMapping("/{id}/eliminarRol")
  public ResponseEntity<ApiResponse<UsuarioResponseDto>> eliminarRol(@PathVariable Long id, @RequestBody RoleDTO role) {
    Usuario user = usuarioService.eliminarRol(id, role.getRole());
    UsuarioResponseDto userResponseDto = usuarioMapper.toResponseDto(user);
    ApiResponse<UsuarioResponseDto> response = new ApiResponse<UsuarioResponseDto>(
        true, "Rol eliminado con exito!", 200, userResponseDto);
    return ResponseEntity.status(HttpStatus.OK.value()).body(response);
  }

  // actualizar el usuario principal
  @PostMapping("/updatePerfil")
  public ResponseEntity<?> modificarUsuarioLogueado(@RequestBody Usuario usuario) {
    Usuario modificado = usuarioService.actualizarPerfil(usuario);
    if (modificado != null)
      return ResponseEntity.ok(modificado);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado con Usuario");
  }

}