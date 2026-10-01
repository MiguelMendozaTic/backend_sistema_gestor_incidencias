package utp.edu.sistema_gestor_incidencias.model;

import java.util.Date;
import jakarta.validation.constraints.NotNull;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "seguimientos")
public class Seguimiento {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long idSeguimiento;
	@NotNull
	@ManyToOne
	@JoinColumn(name="incidencia_id")
	private Incidencia incidencia;
	@NotNull
	private String comentario;
	@NotNull
	private Date fecha;
	@NotNull
	@Enumerated(EnumType.STRING)
	private Estado estado;
	@NotNull
	@ManyToOne
	@JoinColumn(name="usuario_id")
	private Usuario usuario;
	
	public Seguimiento(Long idSeguimiento, Incidencia incidencia, String comentario, Date fecha, Estado estado, Usuario usuario) {
		super();
		this.idSeguimiento = idSeguimiento;
		this.incidencia = incidencia;
		this.comentario = comentario;
		this.fecha = fecha;
		this.estado = estado;
		this.usuario = usuario;
	}
	
	
	public Seguimiento() {}


	public Long getIdSeguimiento() {
		return idSeguimiento;
	}


	public void setIdSeguimiento(Long idSeguimiento) {
		this.idSeguimiento = idSeguimiento;
	}


	public Incidencia getIncidencia() {
		return incidencia;
	}


	public void setIncidencia(Incidencia incidencia) {
		this.incidencia = incidencia;
	}


	public String getComentario() {
		return comentario;
	}


	public void setComentario(String comentario) {
		this.comentario = comentario;
	}


	public Date getFecha() {
		return fecha;
	}


	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}


	public Estado getEstado() {
		return estado;
	}


	public void setEstado(Estado estado) {
		this.estado = estado;
	}


	public Usuario getUsuario() {
		return usuario;
	}


	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	
}
