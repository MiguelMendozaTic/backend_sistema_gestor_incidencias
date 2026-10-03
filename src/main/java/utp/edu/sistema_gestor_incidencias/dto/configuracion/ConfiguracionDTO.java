package utp.edu.sistema_gestor_incidencias.dto.configuracion;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ConfiguracionDTO {

	@NotNull(message = "El intervalo es obligatorio")
	@Min(value = 0, message = "El intervalo no puede ser negativo")
	@Max(value = 600, message = "El intervalo máximo es 600 segundos (10 minutos)")
	private Integer intervaloRefresco;

	public ConfiguracionDTO() {
	}

	public ConfiguracionDTO(Integer intervaloRefresco) {
		this.intervaloRefresco = intervaloRefresco;
	}

	/** Solo 0 (desactivado) o 10 segundos en adelante, para no sobrecargar el servidor. */
	@JsonIgnore
	@AssertTrue(message = "El intervalo debe ser 0 (desactivado) o de al menos 10 segundos")
	public boolean isIntervaloValido() {
		return intervaloRefresco == null || intervaloRefresco == 0 || intervaloRefresco >= 10;
	}

	public Integer getIntervaloRefresco() {
		return intervaloRefresco;
	}

	public void setIntervaloRefresco(Integer intervaloRefresco) {
		this.intervaloRefresco = intervaloRefresco;
	}
}
