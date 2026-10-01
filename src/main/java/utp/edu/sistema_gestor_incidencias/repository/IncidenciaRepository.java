package utp.edu.sistema_gestor_incidencias.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utp.edu.sistema_gestor_incidencias.dto.usuario.TecnicosDTO;
import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;
import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.model.Usuario;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
	Page<Incidencia> findAllByOrderByFechaCreacionDesc(Pageable pageable);

	Page<Incidencia> findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrderByFechaCreacionDesc(
			String titulo,
			String descripcion,
			Pageable pageable);

	@Query("SELECT i FROM Incidencia i " +
			"WHERE i.usuario = :usuario OR i.tecnico = :usuario " +
			"ORDER BY i.fechaCreacion DESC")
	Page<Incidencia> findByUsuarioOrTecnico(@Param("usuario") Usuario usuario, Pageable pageable);

	@Query("SELECT i FROM Incidencia i " +
			"WHERE (i.usuario = :usuario OR i.tecnico = :tecnico) " +
			"AND (LOWER(i.titulo) LIKE LOWER(CONCAT('%', :texto, '%')) " +
			"     OR LOWER(i.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
			"ORDER BY i.fechaCreacion DESC")
	Page<Incidencia> buscarPorUsuarioOTecnicoYTexto(
			@Param("usuario") Usuario usuario,
			@Param("tecnico") Usuario tecnico,
			@Param("texto") String texto,
			Pageable pageable);

	List<Incidencia> findByUsuario(Usuario usuario);

	List<Incidencia> findByTecnico(Usuario tecnico);

	long countByEstado(EstadoIncidencia estado);

	// Alternativa: obtener todos los conteos de un solo golpe (GROUP BY)
	@Query("SELECT i.estado, COUNT(i) FROM Incidencia i GROUP BY i.estado")
	List<Object[]> countGroupByEstado();

	long countByUsuarioOrTecnico(Usuario usuario, Usuario tecnico);

	@Query("SELECT COUNT(i) FROM Incidencia i WHERE (i.usuario = :usuario OR i.tecnico = :tecnico) AND i.estado = :estado")
	long countByUsuarioOrTecnicoAndEstado(@Param("usuario") Usuario usuario,
			@Param("tecnico") Usuario tecnico,
			@Param("estado") EstadoIncidencia estado);

	// Últimos 5 incidencias registrados
	List<Incidencia> findTop5ByUsuarioOrTecnicoOrderByFechaCreacionDesc(Usuario usuario, Usuario tecnico);

	@Query("SELECT new utp.edu.sistema_gestor_incidencias.dto.usuario.TecnicosDTO(u.id, u.nombre, COUNT(i)) " +
			"FROM Usuario u " +
			"JOIN u.roles r " +
			"LEFT JOIN Incidencia i ON i.tecnico = u AND i.estado IN ('PENDIENTE', 'ABIERTA') " +
			"WHERE r.name LIKE '%TECNICO%' " +
			"GROUP BY u.id, u.nombre")
	List<TecnicosDTO> obtenerTecnicosConIncidenciasPendientes();
}
