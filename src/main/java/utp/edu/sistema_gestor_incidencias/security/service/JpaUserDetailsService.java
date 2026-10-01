package utp.edu.sistema_gestor_incidencias.security.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


@Service
public class JpaUserDetailsService implements UserDetailsService{

	private UsuarioRepository usuarioRepository;
	
	JpaUserDetailsService(UsuarioRepository usuarioRepository){
		this.usuarioRepository =  usuarioRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<Usuario> optionalUser  = usuarioRepository.findByUsername(username);
		if(optionalUser.isEmpty()) {
			throw new UsernameNotFoundException( String.format("El usuario %s no existe en el sistema", username));
		}
		Usuario user = optionalUser.orElseThrow();
		List<GrantedAuthority> authorities = user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName()))
				.collect(Collectors.toList());
		
		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPasswordHash(),
				true,
				true,
				true,
				true,
				authorities
				
				);
	}

}
