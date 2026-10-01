package utp.edu.sistema_gestor_incidencias.dto.usuario;

public class TecnicosDTO {
  private Long id;
  private String nombre;
  private Long cantidadIncidenciaPendiente;

  public TecnicosDTO(){}

  public TecnicosDTO(Long id, String nombre, Long cantidadIncidenciaPendiente) {
    this.id = id;
    this.nombre = nombre;
    this.cantidadIncidenciaPendiente = cantidadIncidenciaPendiente;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getNombre() {
    return nombre;
  }

  public void setUsername(String nombre) {
    this.nombre = nombre;
  }

  public Long getCantidadIncidenciaPendiente() {
    return cantidadIncidenciaPendiente;
  }

  public void setCantidadIncidenciaPendiente(Long cantidadIncidenciaPendiente) {
    this.cantidadIncidenciaPendiente = cantidadIncidenciaPendiente;
  }

}
