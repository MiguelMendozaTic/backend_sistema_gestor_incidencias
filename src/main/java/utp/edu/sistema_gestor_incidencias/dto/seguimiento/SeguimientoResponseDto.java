package utp.edu.sistema_gestor_incidencias.dto.seguimiento;

import java.util.Date;

import utp.edu.sistema_gestor_incidencias.enums.Estado;

public class SeguimientoResponseDto {
	private Long id;
	private String comentario;
	private Estado estado;
	private Long incidenciaId;
	private Date fecha;
	private String nombreUsuario;

	public SeguimientoResponseDto(){}

	public SeguimientoResponseDto(Long id, String comentario, Estado estado, Long incidenciaId, Date fecha,
			String nombreUsuario) {
		this.id = id;
		this.comentario = comentario;
		this.estado = estado;
		this.incidenciaId = incidenciaId;
		this.fecha = fecha;
		this.nombreUsuario = nombreUsuario;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getIncidenciaId() {
		return incidenciaId;
	}

	public void setIncidenciaId(Long incidenciaId) {
		this.incidenciaId = incidenciaId;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}
	
	

}
