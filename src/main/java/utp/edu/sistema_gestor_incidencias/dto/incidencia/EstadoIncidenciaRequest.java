package utp.edu.sistema_gestor_incidencias.dto.incidencia;

import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;

public class EstadoIncidenciaRequest {
  private EstadoIncidencia estado;
  private long idIncidencia;
  
  public EstadoIncidenciaRequest(EstadoIncidencia estado, long idIncidencia) {
    this.estado = estado;
    this.idIncidencia = idIncidencia;
  }

  public EstadoIncidencia getEstado() {
    return estado;
  }

  public void setEstado(EstadoIncidencia estado) {
    this.estado = estado;
  }

  public long getIdIncidencia() {
    return idIncidencia;
  }

  public void setIdIncidencia(long idIncidencia) {
    this.idIncidencia = idIncidencia;
  }

  
}
