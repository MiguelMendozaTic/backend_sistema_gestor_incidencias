package utp.edu.sistema_gestor_incidencias.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.model.Seguimiento;
import utp.edu.sistema_gestor_incidencias.model.Usuario;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {
	Page<Seguimiento> findAllByOrderByFechaDesc(Pageable pageable) ;
	
	List<Seguimiento> findByIncidencia(Incidencia incidencia);

	@Query("SELECT s FROM Seguimiento s " +
	           "WHERE s.incidencia = :incidencia " +
	           "AND (s.usuario = :usuario OR s.usuario = :tecnico)")
	List<Seguimiento> findByIncidenciaAndUsuarioOrTecnico(@Param("incidencia") Incidencia incidencia,
	                                                          @Param("usuario") Usuario usuario,
	                                                          @Param("tecnico") Usuario tecnico);
}
