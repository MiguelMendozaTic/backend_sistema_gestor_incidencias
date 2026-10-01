package utp.edu.sistema_gestor_incidencias.dto.dashboard;

import utp.edu.sistema_gestor_incidencias.enums.Estado;

public class LatestUserDTO {
  private String nombre;
  private Estado estado; // 'ACTIVO', 'INACTIVO', etc.

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Estado getEstado() {
    return estado;
  }

  public void setEstado(Estado estado) {
    this.estado = estado;
  }

  

}
