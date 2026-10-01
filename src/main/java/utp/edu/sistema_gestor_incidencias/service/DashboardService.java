package utp.edu.sistema_gestor_incidencias.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import utp.edu.sistema_gestor_incidencias.dto.dashboard.AreaCountDTO;
import utp.edu.sistema_gestor_incidencias.dto.dashboard.DashboardSummaryDTO;
import utp.edu.sistema_gestor_incidencias.dto.dashboard.IncidentStatsDTO;
import utp.edu.sistema_gestor_incidencias.dto.dashboard.LatestUserDTO;
import utp.edu.sistema_gestor_incidencias.dto.dashboard.UserStatsDTO;
import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;
import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.IncidenciaRepository;
import utp.edu.sistema_gestor_incidencias.repository.UsuarioRepository;

@Service
public class DashboardService {

  private IncidenciaRepository incidenciaRepo;
  private UsuarioRepository usuarioRepo;
  private UsuarioService usuarioService;

  public DashboardService(IncidenciaRepository incidenciaRepo, UsuarioRepository usuarioRepo,
      UsuarioService usuarioService) {
    this.incidenciaRepo = incidenciaRepo;
    this.usuarioRepo = usuarioRepo;
    this.usuarioService = usuarioService;
  }

  public DashboardSummaryDTO getDashboardSummary() {
    DashboardSummaryDTO dto = new DashboardSummaryDTO();

    // --- 1. Estadísticas de Incidencias ---
    IncidentStatsDTO incidentStats = new IncidentStatsDTO();
    incidentStats.setTotal(incidenciaRepo.count());
    incidentStats.setAbiertas(incidenciaRepo.countByEstado(EstadoIncidencia.ABIERTO));
    incidentStats.setEnProgreso(incidenciaRepo.countByEstado(EstadoIncidencia.PENDIENTE));
    incidentStats.setCerradas(incidenciaRepo.countByEstado(EstadoIncidencia.CERRADO));
    dto.setIncidentStats(incidentStats);

    // --- 2. Estadísticas de Usuarios por Área ---
    UserStatsDTO userStats = new UserStatsDTO();
    userStats.setTotalUsuarios(usuarioRepo.countAllByEstado(Estado.ACTIVO)); // Solo activos

    List<Object[]> areaResults = usuarioRepo.countGroupByArea();
    List<AreaCountDTO> areas = new ArrayList<>();
    for (Object[] row : areaResults) {
      AreaCountDTO areaDTO = new AreaCountDTO();
      areaDTO.setArea((Area) row[0]);
      areaDTO.setCantidad((Long) row[1]);
      areas.add(areaDTO);
    }
    userStats.setAreas(areas);
    dto.setUserStats(userStats);

    // --- 3. Últimos 5 Usuarios Registrados ---
    List<Usuario> topUsers = usuarioRepo.findTop5ByOrderByIdDesc();
    List<LatestUserDTO> latest = new ArrayList<>();
    for (Usuario u : topUsers) {
      LatestUserDTO lu = new LatestUserDTO();
      lu.setNombre(u.getNombre());
      lu.setEstado(u.getEstado()); 
      latest.add(lu);
    }
    dto.setLatestUsers(latest);

    return dto;
  }

  public Map<String, Object> getDashboardUsuarios() {
  
    Usuario usuarioLogueado = usuarioService.obtenerUsuarioSession()
        .orElseThrow(() -> new UsernameNotFoundException("El usuario no fue encontrado"));
    // --- 1. Estadísticas de Incidencias ---
    IncidentStatsDTO incidentStats = new IncidentStatsDTO();
    incidentStats.setTotal(incidenciaRepo.countByUsuarioOrTecnico(usuarioLogueado, usuarioLogueado));
    incidentStats.setAbiertas(incidenciaRepo.countByUsuarioOrTecnicoAndEstado(usuarioLogueado,usuarioLogueado, EstadoIncidencia.ABIERTO));
    incidentStats.setEnProgreso(incidenciaRepo.countByUsuarioOrTecnicoAndEstado(usuarioLogueado,usuarioLogueado, EstadoIncidencia.PENDIENTE));
    incidentStats.setCerradas(incidenciaRepo.countByUsuarioOrTecnicoAndEstado(usuarioLogueado,usuarioLogueado, EstadoIncidencia.CERRADO));

    Map<String, Object> response = new HashMap<>();
    response.put("incidentStats", incidentStats);

    List<Incidencia> top5Incidencias = incidenciaRepo.findTop5ByUsuarioOrTecnicoOrderByFechaCreacionDesc(usuarioLogueado,usuarioLogueado);
    
    response.put("incidenciasRecientes", top5Incidencias);
    return response;
  }
}
