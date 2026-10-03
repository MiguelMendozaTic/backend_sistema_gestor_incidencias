package utp.edu.sistema_gestor_incidencias.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;
import utp.edu.sistema_gestor_incidencias.exception.IncidenciaNotFoundException;
import utp.edu.sistema_gestor_incidencias.exception.UsuarioNoEncontradoException;
import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.model.Seguimiento;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.IncidenciaRepository;
import utp.edu.sistema_gestor_incidencias.repository.SeguimientoRepository;
import utp.edu.sistema_gestor_incidencias.notificacion.NotificacionIncidenciaEvent;
import utp.edu.sistema_gestor_incidencias.notificacion.NotificacionIncidenciaEvent.TipoNotificacion;

@Service
public class SeguimientoService {

  private SeguimientoRepository seguimientoRepository;

  private IncidenciaRepository incidenciaRepository;

  private UsuarioService usuarioService;

  private ApplicationEventPublisher eventPublisher;

  public SeguimientoService(SeguimientoRepository seguimientoRepository, IncidenciaRepository incidenciaRepository,
      UsuarioService usuarioService, ApplicationEventPublisher eventPublisher) {
    this.seguimientoRepository = seguimientoRepository;
    this.incidenciaRepository = incidenciaRepository;
    this.usuarioService = usuarioService;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Comentario escrito por un usuario: se guarda y se notifica al solicitante.
   * Una incidencia CERRADA queda en solo lectura; únicamente el administrador puede comentar.
   */
  public Seguimiento crearSeguimiento(Seguimiento seguimiento) {
    Incidencia incidencia = incidenciaRepository.findById(seguimiento.getIncidencia().getId())
        .orElseThrow(() -> new IncidenciaNotFoundException("Incidencia no encontrada"));
    if (incidencia.getEstado() == EstadoIncidencia.CERRADO) {
      Usuario usuario = usuarioService.obtenerUsuarioSession()
          .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no se ha encontrado"));
      boolean isAdmin = usuario.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
      if (!isAdmin) {
        throw new IllegalStateException(
            "La incidencia está cerrada: no se pueden agregar comentarios. Solo el administrador puede reabrirla.");
      }
    }
    Seguimiento guardado = registrarEventoSistema(seguimiento);
    eventPublisher.publishEvent(NotificacionIncidenciaEvent.de(TipoNotificacion.SEGUIMIENTO,
        guardado.getIncidencia(), guardado.getUsuario(), guardado.getFecha(),
        "Comentario: " + guardado.getComentario()));
    return guardado;
  }

  /**
   * Guarda el seguimiento sin notificar. Lo usa IncidenciaService para el
   * historial automático (creación, estado, técnico), que ya notifica por su cuenta.
   */
  public Seguimiento registrarEventoSistema(Seguimiento seguimiento) {

    Optional<Incidencia> incidencia = incidenciaRepository.findById(seguimiento.getIncidencia().getId());
    if (!incidencia.isPresent()) {
      throw new IncidenciaNotFoundException("Incidencia no encontrada");
    }
    Incidencia incidenciaEncontrada = incidencia.get();
    
    Usuario usuario = usuarioService.obtenerUsuarioSession()
        .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no se ha encontrado"));
    
    // Quien la registró, su solicitante, el técnico asignado o el admin
    var isAdmin = usuario.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

    if (incidenciaEncontrada.participa(usuario) || isAdmin) {
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

    // Quien la registró, su solicitante, el técnico asignado o el admin
    var isAdmin = usuario.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

    if (incidencia.participa(usuario) || isAdmin) {
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