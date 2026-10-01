package utp.edu.sistema_gestor_incidencias.mappers;

import org.springframework.stereotype.Component;

import utp.edu.sistema_gestor_incidencias.dto.usuario.UsuarioDTO;
import utp.edu.sistema_gestor_incidencias.dto.usuario.UsuarioResponseDto;
import utp.edu.sistema_gestor_incidencias.model.Usuario;

@Component
public class UsuarioMapper {
	public Usuario toEntity(UsuarioDTO usuarioDTO) {
		if(usuarioDTO == null) {
			return null;
		}		
		Usuario user = new Usuario();
		user.setUsername(usuarioDTO.getUsername());
		user.setNombre(usuarioDTO.getNombre());
		user.setPasswordHash(usuarioDTO.getPassword());
		user.setCorreo(usuarioDTO.getCorreo());
		user.setArea(usuarioDTO.getArea());
		return user;
	}
	
	public UsuarioResponseDto toResponseDto(Usuario usuario) {
		if(usuario == null) {
			return null;
		}	
		UsuarioResponseDto newResponse = new UsuarioResponseDto();
		newResponse.setCorreo(usuario.getCorreo());
		newResponse.setUsername(usuario.getUsername());
		newResponse.setRoles(usuario.getRoles());
		return newResponse;
	}; 

}
