package utp.edu.sistema_gestor_incidencias.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.enums.TipoEquipo;
import utp.edu.sistema_gestor_incidencias.model.Equipo;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByEstado(Estado estado);

    List<Equipo> findByArea(Area area);

    List<Equipo> findByTipo(TipoEquipo tipo);

    Page<Equipo> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(
            String nombre, String codigo, Pageable pageable);

    Page<Equipo> findAllByOrderByNombreAsc(Pageable pageable);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    /** Búsqueda por nombre o código, con filtro opcional de estado. */
    @Query("SELECT e FROM Equipo e " +
            "WHERE (:estado IS NULL OR e.estado = :estado) " +
            "AND (:texto = '' OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR LOWER(e.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
            "ORDER BY e.nombre ASC")
    Page<Equipo> buscar(@Param("texto") String texto, @Param("estado") Estado estado, Pageable pageable);

    /**
     * Obtiene el ultimo codigo generado para un prefijo de tipo.
     * Ejemplo: busca todos los codigos que inician con el prefijo y retorna el mayor.
     */
    @Query("SELECT e.codigo FROM Equipo e WHERE e.codigo LIKE CONCAT(:prefijo, '-%') ORDER BY e.codigo DESC")
    List<String> findCodigosByPrefijo(@Param("prefijo") String prefijo);

    long countByTipo(TipoEquipo tipo);
}
