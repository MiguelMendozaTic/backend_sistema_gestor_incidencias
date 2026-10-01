package utp.edu.sistema_gestor_incidencias.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;


@Entity
@Table(name = "usuarios")
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long id;
	@NotNull
    @Column(name="username", nullable = false, unique = true)
	private String username;
	@NotNull
	@Column(name = "password_hash")
	private String passwordHash;
	@NotNull
	private String nombre;
	@NotNull
	@Email
    @Column(name="correo", nullable = false, unique = true)
	private String correo;
	@NotNull
	@Enumerated(EnumType.STRING)
	private Estado estado;
	@NotNull
	@Enumerated(EnumType.STRING)
	private Area area;
	
	@ManyToMany(fetch = FetchType.EAGER, cascade= {CascadeType.MERGE, CascadeType.PERSIST})
	@JoinTable(
			name = "users_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"),
			uniqueConstraints = @UniqueConstraint(columnNames = {"user_id" , "role_id"})
	)
	private Set<Role> roles;
	
	public Usuario() {
		roles = new HashSet<>();
	}

	public Usuario(Long id, @NotNull String username,String passwordHash, @NotNull String nombre, @NotNull @Email String correo,
			@NotNull Estado estado, @NotNull Area area, Set<Role> roles) {
		this.id = id;
		this.username = username;
		this.nombre = nombre;
		this.correo = correo;
		this.estado = estado;
		this.area = area;
		this.roles = roles;
		this.passwordHash = passwordHash;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void addRole(Role role){
		this.roles.add(role);
	}
	
}
