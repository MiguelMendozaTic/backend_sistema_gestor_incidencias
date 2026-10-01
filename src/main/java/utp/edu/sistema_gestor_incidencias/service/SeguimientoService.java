package utp.edu.sistema_gestor_incidencias.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import utp.edu.sistema_gestor_incidencias.exception.IncidenciaNotFoundException;
import utp.edu.sistema_gestor_incidencias.exception.UsuarioNoEncontradoException;
import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.model.Seguimiento;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.IncidenciaRepository;
import utp.edu.sistema_gestor_incidencias.repository.SeguimientoRepository;

@Service
public class SeguimientoService {

  private SeguimientoRepository seguimientoRepository;

  private IncidenciaRepository incidenciaRepository;

  private UsuarioService usuarioService;

  public SeguimientoService(SeguimientoRepository seguimientoRepository, IncidenciaRepository incidenciaRepository,
      UsuarioService usuarioService) {
    this.seguimientoRepository = seguimientoRepository;
    this.incidenciaRepository = incidenciaRepository;
    this.usuarioService = usuarioService;
  }

  public Seguimiento crearSeguimiento(Seguimiento seguimiento) {

    Optional<Incidencia> incidencia = incidenciaRepository.findById(seguimiento.getIncidencia().getId());
    if (!incidencia.isPresent()) {
      throw new IncidenciaNotFoundException("Incidencia no encontrada");
    }
    Incidencia incidenciaEncontrada = incidencia.get();
    
    Usuario usuario = usuarioService.obtenerUsuarioSession()
        .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no se ha encontrado"));
    
    Long idSession = usuario.getId();
    var mismoUsuario = Objects.equals(idSession, incidenciaEncontrada.getUsuario().getId());
    var misTecnico = incidenciaEncontrada.getTecnico() != null && Objects.equals(idSession, incidenciaEncontrada.getTecnico().getId());
    var isAdmin = usuario.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

    if (mismoUsuario || misTecnico || isAdmin) {
      seguimiento.setUsuario(usuario);
    }else{
      throw new AccessDeniedException("Usuario no autorizado para crear");
    }

    seguimiento.setIncidencia(incidenciaEncontrada);
    Date fechaActual = new Date();
    seguimiento.setFecha(fechaActual);

    return seguimientoRepository.save(seguimiento);
  }

  public Seguimiento modificarSeguimiento(Long id, Seguimiento datosNuevos) {
    var incidencia = incidenciaRepository.findById(datosNuevos.getIncidencia().getId());

    if (!incidencia.isPresent()) {

      throw new IncidenciaNotFoundException("Incidencia no encontrada");
    }

    datosNuevos.setIncidencia(incidencia.get());

    Optional<Seguimiento> encontrado = seguimientoRepository.findById(id);
    ;
    if (encontrado.isPresent()) {
      Seguimiento s = encontrado.get();
      s.setComentario(datosNuevos.getComentario());
      s.setEstado(datosNuevos.getEstado());
      return seguimientoRepository.save(s);
    }
    return null;
  }

  public List<Seguimiento> listarSeguimientos() {
    return seguimientoRepository.findAll();
  }

  public Optional<Seguimiento> obtenerSeguimiento(Long id) {
    return seguimientoRepository.findById(id);
  }

  public Page<Seguimiento> listarSeguimientosPaginado(Pageable pageable) {
    return seguimientoRepository.findAllByOrderByFechaDesc(pageable);
  }

  public List<Seguimiento> misSeguimientos(Long id) {
    Incidencia incidencia = incidenciaRepository.findById(id)
        .orElseThrow(() -> new IncidenciaNotFoundException("Incidencia no encontrada"));
    Usuario usuario = usuarioService.obtenerUsuarioSession()
        .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no se ha encontrado"));

    Long idSession = usuario.getId();

    var mismoUsuario = Objects.equals(idSession, incidencia.getUsuario().getId());
    var misTecnico = incidencia.getTecnico() != null && Objects.equals(idSession, incidencia.getTecnico().getId());
    var isAdmin = usuario.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

    if (mismoUsuario || misTecnico || isAdmin) {
      return seguimientoRepository.findByIncidencia(incidencia);
    }

    throw new AccessDeniedException("Usuario no autorizado");

    /*
     * return seguimientoRepository.findByIncidenciaAndUsuarioOrTecnico(incidencia,
     * incidencia.getUsuario(),
     * incidencia.getTecnico());
     */
  }
}