package utp.edu.sistema_gestor_incidencias.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utp.edu.sistema_gestor_incidencias.service.auth.AuthService;

/**
 * Validaciones de disponibilidad que usa el formulario de alta del panel admin.
 * No hay registro público: solo el administrador da de alta usuarios (POST /api/usuario).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
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
