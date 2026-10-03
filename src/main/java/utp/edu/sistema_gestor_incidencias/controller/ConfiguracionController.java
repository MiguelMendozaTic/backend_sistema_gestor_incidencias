package utp.edu.sistema_gestor_incidencias.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import utp.edu.sistema_gestor_incidencias.dto.configuracion.ConfiguracionDTO;
import utp.edu.sistema_gestor_incidencias.service.ConfiguracionService;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

	private final ConfiguracionService configuracionService;

	public ConfiguracionController(ConfiguracionService configuracionService) {
		this.configuracionService = configuracionService;
	}

	/** Cualquier usuario autenticado la lee (la usa la recarga automática de las vistas). */
	@GetMapping
	public ResponseEntity<ConfiguracionDTO> obtener() {
		return ResponseEntity.ok(configuracionService.obtener());
	}

	/** Solo el administrador la modifica (regla en SpringSecurityConfig). */
	@PutMapping
	public ResponseEntity<ConfiguracionDTO> actualizar(@Valid @RequestBody ConfiguracionDTO dto) {
		return ResponseEntity.ok(configuracionService.actualizar(dto));
	}
}
