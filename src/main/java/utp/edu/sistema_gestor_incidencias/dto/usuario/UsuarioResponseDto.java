package utp.edu.sistema_gestor_incidencias.dto.usuario;

import java.util.Set;

import utp.edu.sistema_gestor_incidencias.model.Role;

public class UsuarioResponseDto {
	private String username;
	private String correo;
	private Set<Role> roles;
	
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getCorreo() {
		return correo;
	}
	public void setCorreo(String correo) {
		this.correo = correo;
	}
	
}
