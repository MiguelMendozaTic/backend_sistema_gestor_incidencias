package utp.edu.sistema_gestor_incidencias.dto.dashboard;

import utp.edu.sistema_gestor_incidencias.enums.Area;

public class AreaCountDTO {
  private Area area;
  private long cantidad;

  
  public long getCantidad() {
    return cantidad;
  }

  public void setCantidad(long cantidad) {
    this.cantidad = cantidad;
  }

  public Area getArea() {
    return area;
  }

  public void setArea(Area area) {
    this.area = area;
  }
}
