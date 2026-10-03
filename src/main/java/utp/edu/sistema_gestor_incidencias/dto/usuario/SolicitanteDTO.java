package utp.edu.sistema_gestor_incidencias.dto.usuario;

import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.model.Usuario;

/** Datos mínimos de un usuario para elegirlo como solicitante de una incidencia. */
public record SolicitanteDTO(Long id, String nombre, String username, String correo, Area area) {

	public static SolicitanteDTO de(Usuario u) {
		return new SolicitanteDTO(u.getId(), u.getNombre(), u.getUsername(), u.getCorreo(), u.getArea());
	}
}
