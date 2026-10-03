package utp.edu.sistema_gestor_incidencias.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "incidencias")
public class Incidencia {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotNull
	private String titulo;
	@NotNull
	private String descripcion;
	@NotNull
	@Enumerated(EnumType.STRING)
	private EstadoIncidencia estado;
	@NotNull
	@ManyToOne
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;
	private Date fechaCreacion;
	@ManyToOne
	@JoinColumn(name = "tecnico_id")
	private Usuario tecnico;
	@ManyToOne
	@JoinColumn(name = "equipo_id")
	private Equipo equipo;
	/** Usuario que solicita la atención; recibe las notificaciones en su correo registrado. */
	@ManyToOne
	@JoinColumn(name = "solicitante_id")
	private Usuario solicitante;
	/** Se llenan al pasar a CERRADO y se limpian si la incidencia se reabre. */
	private Date fechaCierre;
	@ManyToOne
	@JoinColumn(name = "tecnico_cierre_id")
	private Usuario tecnicoCierre;

	public Incidencia() {
	}

	public Incidencia(Long id, String titulo, String descripcion, EstadoIncidencia estado, Usuario usuario,
			Date fechaCreacion, Usuario tecnico) {
		super();
		this.id = id;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.estado = estado;
		this.usuario = usuario;
		this.tecnico = tecnico;
		this.fechaCreacion = fechaCreacion;
	}

	public Incidencia(Long id, String titulo, String descripcion, EstadoIncidencia estado, Usuario usuario,
			Date fechaCreacion, Usuario tecnico, Equipo equipo) {
		this(id, titulo, descripcion, estado, usuario, fechaCreacion, tecnico);
		this.equipo = equipo;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

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

	public EstadoIncidencia getEstado() {
		return estado;
	}

	public void setEstado(EstadoIncidencia estado) {
		this.estado = estado;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Usuario getTecnico() {
		return tecnico;
	}

	public void setTecnico(Usuario tecnico) {
		this.tecnico = tecnico;
	}

	public Equipo getEquipo() {
		return equipo;
	}

	public void setEquipo(Equipo equipo) {
		this.equipo = equipo;
	}

	public Usuario getSolicitante() {
		return solicitante;
	}

	public void setSolicitante(Usuario solicitante) {
		this.solicitante = solicitante;
	}

	public Date getFechaCierre() {
		return fechaCierre;
	}

	public void setFechaCierre(Date fechaCierre) {
		this.fechaCierre = fechaCierre;
	}

	public Usuario getTecnicoCierre() {
		return tecnicoCierre;
	}

	public void setTecnicoCierre(Usuario tecnicoCierre) {
		this.tecnicoCierre = tecnicoCierre;
	}

	/** true si el usuario la registró, es su solicitante o es el técnico asignado. */
	public boolean participa(Usuario u) {
		if (u == null || u.getId() == null) return false;
		return mismoId(usuario, u) || mismoId(solicitante, u) || mismoId(tecnico, u);
	}

	/** true si el usuario es el técnico asignado. */
	public boolean esTecnicoAsignado(Usuario u) {
		return u != null && u.getId() != null && mismoId(tecnico, u);
	}

	private static boolean mismoId(Usuario a, Usuario b) {
		return a != null && b.getId().equals(a.getId());
	}

}
