package utp.edu.sistema_gestor_incidencias.dto.seguimiento;


import jakarta.validation.constraints.NotNull;
import utp.edu.sistema_gestor_incidencias.enums.Estado;

public class SeguimientoDTO {
	@NotNull
	private String comentario;
	@NotNull
	private Estado estado;
	@NotNull
	private Long idIncidencia;
	
	public SeguimientoDTO(){};
	
	public SeguimientoDTO(@NotNull String comentario, @NotNull Estado estado, Long idIncidencia) {
		this.comentario = comentario;
		this.estado = estado;
		this.idIncidencia = idIncidencia;
	}
	
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	public Estado getEstado() {
		return estado;
	}
	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public Long getIdIncidencia() {
		return idIncidencia;
	}

	public void setIdIncidencia(Long idIncidencia) {
		this.idIncidencia = idIncidencia;
	}
	
	
}
