package utp.edu.sistema_gestor_incidencias.dto.incidencia;

public class IncidenciaDTO {

    private String titulo;
    private String descripcion;
    private Long equipoId;
    /** Usuario solicitante (opcional): si no llega, es el usuario que registra la incidencia. */
    private Long solicitanteId;

    public IncidenciaDTO() {}

	public IncidenciaDTO(String titulo, String descripcion) {
		this.titulo = titulo;
		this.descripcion = descripcion;
	}

	public IncidenciaDTO(String titulo, String descripcion, Long equipoId) {
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.equipoId = equipoId;
	}

	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public Long getEquipoId() {
		return equipoId;
	}
	public void setEquipoId(Long equipoId) {
		this.equipoId = equipoId;
	}
	public Long getSolicitanteId() {
		return solicitanteId;
	}
	public void setSolicitanteId(Long solicitanteId) {
		this.solicitanteId = solicitanteId;
	}
}

