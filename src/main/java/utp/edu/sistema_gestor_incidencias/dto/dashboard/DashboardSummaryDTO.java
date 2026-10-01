package utp.edu.sistema_gestor_incidencias.dto.dashboard;

import java.util.List;

public class DashboardSummaryDTO {
  private IncidentStatsDTO incidentStats;
  private UserStatsDTO userStats;
  private List<LatestUserDTO> latestUsers;

  public IncidentStatsDTO getIncidentStats() {
    return incidentStats;
  }

  public void setIncidentStats(IncidentStatsDTO incidentStats) {
    this.incidentStats = incidentStats;
  }

  public UserStatsDTO getUserStats() {
    return userStats;
  }

  public void setUserStats(UserStatsDTO userStats) {
    this.userStats = userStats;
  }

  public List<LatestUserDTO> getLatestUsers() {
    return latestUsers;
  }

  public void setLatestUsers(List<LatestUserDTO> latestUsers) {
    this.latestUsers = latestUsers;
  }

}

