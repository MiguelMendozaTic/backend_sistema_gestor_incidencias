package utp.edu.sistema_gestor_incidencias.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import utp.edu.sistema_gestor_incidencias.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(String name);
}
