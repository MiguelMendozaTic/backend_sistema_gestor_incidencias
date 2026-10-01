package utp.edu.sistema_gestor_incidencias.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import utp.edu.sistema_gestor_incidencias.enums.Area;

public class UsuarioDTO {
	
  @NotBlank(message = "El nombre de usuario es obligatorio")
  @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
  private String username;

  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 5, message = "La contraseña debe tener al menos 5 caracteres")
  private String password;

  @Size(min = 3, max = 20, message = "El nombre debe tener entre 3 y 35 caracteres")
  @NotBlank(message = "El nombre no puede estar vacío")
  private String nombre;

  @NotBlank(message = "El correo es obligatorio")
  @Email(message = "El formato del correo electrónico no es válido")
  private String correo;

  @NotNull(message = "El área es obligatoria")
  private Area area;

	private String rol;
	
	public UsuarioDTO() {} 

	public UsuarioDTO(String username, String password, String nombre, String correo,String rol, Area area) {
		this.username = username;
		this.password = password;
		this.nombre = nombre;
		this.correo = correo;
		this.area = area;
		this.rol = rol;
	}
	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	
}
