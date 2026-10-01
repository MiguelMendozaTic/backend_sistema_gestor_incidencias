package utp.edu.sistema_gestor_incidencias.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utp.edu.sistema_gestor_incidencias.dto.dashboard.DashboardSummaryDTO;
import utp.edu.sistema_gestor_incidencias.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/principal")
  public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
    DashboardSummaryDTO data = dashboardService.getDashboardSummary();
    return ResponseEntity.ok(data);
  }

  @GetMapping("/usuario")
  public ResponseEntity<?> getIncidenciaUsuarios() {
    var data = dashboardService.getDashboardUsuarios();
    return ResponseEntity.ok(data);
  }
}
