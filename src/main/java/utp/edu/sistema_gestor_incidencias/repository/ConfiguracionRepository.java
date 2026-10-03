package utp.edu.sistema_gestor_incidencias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import utp.edu.sistema_gestor_incidencias.model.ConfiguracionSistema;

public interface ConfiguracionRepository extends JpaRepository<ConfiguracionSistema, Long> {
}
