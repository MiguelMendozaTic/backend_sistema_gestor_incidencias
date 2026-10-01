package utp.edu.sistema_gestor_incidencias.dto.incidencia;

import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;

public class IncidenciaResponseDTO {
	private Long id;
    private String titulo;
    private EstadoIncidencia  estado;
    private String descripcion;
    private Long usuarioId;
    private Long tecnicoId;

    public IncidenciaResponseDTO() {}
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public Long getUsuarioId() {
		return usuarioId;
	}
	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}
	public Long getTecnicoId() {
		return tecnicoId;
	}
	public void setTecnicoId(Long tecnicoId) {
		this.tecnicoId = tecnicoId;
	}

	public EstadoIncidencia getEstado() {
		return estado;
	}

	public void setEstado(EstadoIncidencia estado) {
		this.estado = estado;
	}
    
    
    
}
