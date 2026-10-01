package utp.edu.sistema_gestor_incidencias.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utp.edu.sistema_gestor_incidencias.dto.incidencia.EstadoIncidenciaRequest;
import utp.edu.sistema_gestor_incidencias.dto.incidencia.IncidenciaDTO;
import utp.edu.sistema_gestor_incidencias.dto.incidencia.IncidenciaRequestTecnico;
import utp.edu.sistema_gestor_incidencias.dto.incidencia.IncidenciaResponseDTO;
import utp.edu.sistema_gestor_incidencias.exception.UsuarioNoEncontradoException;
import utp.edu.sistema_gestor_incidencias.mappers.IncidenciaMapper;
import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.service.IncidenciaService;
import utp.edu.sistema_gestor_incidencias.service.UsuarioService;

@RestController
@RequestMapping("/api/incidencia")
public class IncidenciaController {

  private IncidenciaService incidenteService;

  private UsuarioService usuarioService;

  private IncidenciaMapper incidenciaMapper;

  public IncidenciaController(IncidenciaService incidenteService, UsuarioService usuarioService,
      IncidenciaMapper incidenciaMapper) {
    this.incidenteService = incidenteService;
    this.usuarioService = usuarioService;
    this.incidenciaMapper = incidenciaMapper;
  }

  @PostMapping
  public ResponseEntity<?> crearIncidencia(@RequestBody IncidenciaDTO incidencia) {

    try {
      var newIncidencia = this.incidenteService.crearIncidencia(incidencia);
      IncidenciaResponseDTO response = incidenciaMapper.toResponseDto(newIncidencia);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (IllegalArgumentException e) {

      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(e.getMessage());

    } catch (RuntimeException e) {

      return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(e.getMessage());

    } catch (Exception e) {

      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error interno del servidor");

    }

  }

  @PutMapping("/{id}")
  public ResponseEntity<?> modificarIncidencia(@PathVariable("id") Long id, @RequestBody Incidencia incidencia) {
    if (incidencia.getTecnico() != null && incidencia.getTecnico().getId() != null) {
      Long idTec = incidencia.getTecnico().getId();
      var tecnico = usuarioService.obtenerUsuario(idTec);
      if (!tecnico.isPresent()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado con ese ID");
      }
      incidencia.setTecnico(tecnico.get());
    }
    var newIncidencia = this.incidenteService.modificarIncidencia(id, incidencia);
    return ResponseEntity.ok().body(newIncidencia);
  }

  @GetMapping
  public ResponseEntity<List<Incidencia>> listarIncidencias() {
    return ResponseEntity.ok(incidenteService.listarIncidencias());
  }

  @GetMapping("/misIncidencias")
  public ResponseEntity<?> misIncidencias() {
    try {
      var incidencias = incidenteService.misIncidencias();
      List<IncidenciaResponseDTO> dtos = new ArrayList<IncidenciaResponseDTO>();
      for (Incidencia incidencia : incidencias) {
        var incidenciaResponseDTO = incidenciaMapper.toResponseDto(incidencia);
        dtos.add(incidenciaResponseDTO);
      }
      return ResponseEntity.ok(dtos);
    } catch (UsuarioNoEncontradoException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

  }

  @GetMapping("/incidenciasPropias")
  public PagedModel<Incidencia> misIncidenciasPropias(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "5") int size,
     @RequestParam(value = "texto", defaultValue = "") String texto) {
    Pageable pageable = PageRequest.of(page, size);
     if(texto.length()>= 2){
        Page<Incidencia> incidencias = this.incidenteService.misIncidenciasBusqueda(texto,pageable);
        return new PagedModel<>(incidencias);
    }
    Page<Incidencia> incidencias = this.incidenteService.misIncidenciasPage(pageable);
    return new PagedModel<>(incidencias);
    
  }

  @GetMapping("/paginado")
  public PagedModel<Incidencia> listarIncidenciasPaginados(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "5") int size,
     @RequestParam(value = "texto", defaultValue = "") String texto) {
    Pageable pageable = PageRequest.of(page, size);
     if(texto.length()>= 2){
        Page<Incidencia> incidencias = this.incidenteService.listarIncidenciasXBusqueda(texto,pageable);
        return new PagedModel<>(incidencias);
    }
    Page<Incidencia> incidencias = this.incidenteService.listarIncidenciasPaginado(pageable);
    return new PagedModel<>(incidencias);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> obtenerIncidencia(@PathVariable Long id) {
    var incidencia = incidenteService.obtenerIncidencia(id);
    if (incidencia.isPresent())
      return ResponseEntity.ok(incidencia.get());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incidencia no encontrada con id: " + id);
  }

  @PostMapping("/actualizarEstado")
  public ResponseEntity<?>  modificarEstado( @RequestBody EstadoIncidenciaRequest dto) {
    Incidencia incidenciaUpdate = incidenteService.modificarEstado(dto);
    if (incidenciaUpdate!=null) {
      return ResponseEntity.ok(incidenciaUpdate);
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incidencia no encontrada con id: " + dto.getIdIncidencia());
  }

  @PostMapping("/actualizarTecnico")
  public ResponseEntity<?>  modificarEstadoTecnico( @RequestBody IncidenciaRequestTecnico dto) {
    Incidencia incidenciaUpdate = incidenteService.cambiarTecnico(dto);
    if (incidenciaUpdate!=null) {
      return ResponseEntity.ok(incidenciaUpdate);
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incidencia no encontrada con id: " + dto.getIdIncidencia());
  }
}