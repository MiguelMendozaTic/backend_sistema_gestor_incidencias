package utp.edu.sistema_gestor_incidencias.service.auth;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.model.Role;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.RoleRepository;
import utp.edu.sistema_gestor_incidencias.repository.UsuarioRepository;

@Service
public class AuthService {
	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;

	public AuthService(UsuarioRepository userRepository, PasswordEncoder passwordEncoder,
			RoleRepository roleRepository) {
		this.usuarioRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
	}

	@Transactional
	public Usuario register(Usuario usuario, String rol) {
		// Validación de negocio 1: Email único
		if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
			throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
		}

		// Validación de negocio 2: Username único
		if (usuarioRepository.existsByUsername(usuario.getUsername())) {
			throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
		}
		String passwordHash = encryptPassword(usuario.getPasswordHash());
		usuario.setPasswordHash(passwordHash);

		Set<Role> roles = new HashSet<Role>();
		if (rol.length()>0) {
			Optional<Role> optionalRole = roleRepository.findByName(rol);
			optionalRole.ifPresent(roles::add);
		} else {
			Optional<Role> optionalRole = roleRepository.findByName("ROLE_EMPLEADO");
			optionalRole.ifPresent(roles::add);
		}

		usuario.setRoles(roles);
		usuario.setEstado(Estado.ACTIVO);

		return usuarioRepository.save(usuario);
	}

	public String encryptPassword(String password) {
		return passwordEncoder.encode(password);
	}

	@Transactional
	public boolean existsByUsername(String username) {
		return usuarioRepository.existsByUsername(username);
	}

	@Transactional
	public boolean existsByCorreo(String correo) {
		return usuarioRepository.existsByCorreo(correo);
	}

}
