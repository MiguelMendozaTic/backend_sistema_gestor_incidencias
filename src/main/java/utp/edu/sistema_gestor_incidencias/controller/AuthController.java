package utp.edu.sistema_gestor_incidencias.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import utp.edu.sistema_gestor_incidencias.dto.ApiResponse;
import utp.edu.sistema_gestor_incidencias.dto.usuario.UsuarioDTO;
import utp.edu.sistema_gestor_incidencias.dto.usuario.UsuarioResponseDto;
import utp.edu.sistema_gestor_incidencias.mappers.UsuarioMapper;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.service.auth.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;
	
	private final UsuarioMapper usuarioMapper;
	
	public AuthController(AuthService authService, UsuarioMapper usuarioMapper) {
		this.authService = authService;
		this.usuarioMapper = usuarioMapper;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<UsuarioResponseDto>> create(@Valid @RequestBody UsuarioDTO userDto){
		
		Usuario user = usuarioMapper.toEntity(userDto);
		Usuario newUser = authService.register(user, userDto.getRol());
		UsuarioResponseDto userResponseDto = usuarioMapper.toResponseDto(newUser);
		ApiResponse<UsuarioResponseDto> response = new ApiResponse<UsuarioResponseDto>(true, "Usuario Creado con exito!", 201, userResponseDto );
		return ResponseEntity.status(HttpStatus.CREATED.value()).body(response); 
		
	}

	@GetMapping("/{username}")
	public ResponseEntity<Map<String, Boolean>> existsByUsername(@PathVariable String username) {
		boolean exists = authService.existsByUsername(username);
		Map<String, Boolean> response = new HashMap<>();
		response.put("exists", exists);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{correo}/validacion")
	public ResponseEntity<Map<String, Boolean>> existsByCorreo(@PathVariable String correo) {
		boolean exists = authService.existsByCorreo(correo);
		Map<String, Boolean> response = new HashMap<>();
		response.put("exists", exists);
		return ResponseEntity.ok(response);
	}
	

	
}
