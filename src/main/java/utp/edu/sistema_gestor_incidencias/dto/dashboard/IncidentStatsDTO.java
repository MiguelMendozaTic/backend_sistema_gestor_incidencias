package utp.edu.sistema_gestor_incidencias.dto.dashboard;

public class IncidentStatsDTO {
  private long total;
  private long abiertas; // estado = 'ABIERTA'
  private long enProgreso; // estado = 'EN_PROCESO'
  private long cerradas; // estado = 'CERRADA'

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public long getAbiertas() {
    return abiertas;
  }

  public void setAbiertas(long abiertas) {
    this.abiertas = abiertas;
  }

  public long getEnProgreso() {
    return enProgreso;
  }

  public void setEnProgreso(long enProgreso) {
    this.enProgreso = enProgreso;
  }

  public long getCerradas() {
    return cerradas;
  }

  public void setCerradas(long cerradas) {
    this.cerradas = cerradas;
  }

}
