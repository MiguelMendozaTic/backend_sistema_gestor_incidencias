package utp.edu.sistema_gestor_incidencias.dto.dashboard;

import java.util.List;

public class UserStatsDTO {
  private long totalUsuarios;
  private List<AreaCountDTO> areas;

  public long getTotalUsuarios() {
    return totalUsuarios;
  }

  public void setTotalUsuarios(long totalUsuarios) {
    this.totalUsuarios = totalUsuarios;
  }

  public List<AreaCountDTO> getAreas() {
    return areas;
  }

  public void setAreas(List<AreaCountDTO> areas) {
    this.areas = areas;
  }
}
