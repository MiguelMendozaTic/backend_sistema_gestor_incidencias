package utp.edu.sistema_gestor_incidencias.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByUsername(String username);

	boolean existsByCorreo(String correo);

	boolean existsByUsername(String username);

	Page<Usuario> findAllByOrderByNombreAsc(Pageable pageable);

	Page<Usuario> findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(String nombre,
			String correo, Pageable pageable);

	@Query("select u from Usuario u left join fetch u.roles where u.id = ?1")
	Optional<Usuario> getUserWithRoles(Long id);

	@Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.name = :nombreRol AND NOT EXISTS " +
			"(SELECT i FROM Incidencia i WHERE i.tecnico = u AND i.estado = 'PENDIENTE')")
	List<Usuario> findTecnicosDisponibles(@Param("nombreRol") String nombreRol);

	// Total de usuarios
	long countAllByEstado(Estado estado); // Si solo quieres activos, usa "ACTIVO"

	// Agrupar por área (para las burbujas del dashboard)
	@Query("SELECT u.area, COUNT(u) FROM Usuario u GROUP BY u.area")
	List<Object[]> countGroupByArea();

	// Últimos 5 usuarios registrados
	List<Usuario> findTop5ByOrderByIdDesc();

	@Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.name = 'ROLE_TECNICO_NIVEL_1' OR r.name = 'ROLE_TECNICO_NIVEL_3' OR r.name = 'ROLE_TECNICO_NIVEL_2 '")
	List<Usuario> listarTecnicos();
}
