package utp.edu.sistema_gestor_incidencias.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Configuración global del sistema: una sola fila con id = 1. */
@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistema {

	public static final long ID = 1L;

	@Id
	private Long id = ID;

	/** Segundos entre recargas automáticas de las vistas; 0 la desactiva. */
	private int intervaloRefresco = 30;

	public ConfiguracionSistema() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getIntervaloRefresco() {
		return intervaloRefresco;
	}

	public void setIntervaloRefresco(int intervaloRefresco) {
		this.intervaloRefresco = intervaloRefresco;
	}
}
