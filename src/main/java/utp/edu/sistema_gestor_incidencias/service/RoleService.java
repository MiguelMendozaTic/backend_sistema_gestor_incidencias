package utp.edu.sistema_gestor_incidencias.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import utp.edu.sistema_gestor_incidencias.model.Role;
import utp.edu.sistema_gestor_incidencias.repository.RoleRepository;

@Service
public class RoleService {
  
	private final RoleRepository roleRepository;

	public RoleService(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	public List<Role> listarRoles() {
		return roleRepository.findAll();
	}

	public Optional<Role> obtenerPorNombre(String name) {
		return roleRepository.findByName(name);
	}

}
