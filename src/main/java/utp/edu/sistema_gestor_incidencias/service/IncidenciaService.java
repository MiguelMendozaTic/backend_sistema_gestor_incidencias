package utp.edu.sistema_gestor_incidencias.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import utp.edu.sistema_gestor_incidencias.dto.incidencia.EstadoIncidenciaRequest;
import utp.edu.sistema_gestor_incidencias.dto.incidencia.IncidenciaDTO;
import utp.edu.sistema_gestor_incidencias.dto.incidencia.IncidenciaRequestTecnico;
import utp.edu.sistema_gestor_incidencias.dto.usuario.TecnicosDTO;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.enums.EstadoIncidencia;
import utp.edu.sistema_gestor_incidencias.exception.IncidenciaNotFoundException;
import utp.edu.sistema_gestor_incidencias.exception.UsuarioNoEncontradoException;
import utp.edu.sistema_gestor_incidencias.model.Equipo;
import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.model.Seguimiento;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.EquipoRepository;
import utp.edu.sistema_gestor_incidencias.repository.IncidenciaRepository;
import utp.edu.sistema_gestor_incidencias.notificacion.NotificacionIncidenciaEvent;
import utp.edu.sistema_gestor_incidencias.notificacion.NotificacionIncidenciaEvent.TipoNotificacion;

@Service
public class IncidenciaService {

	private IncidenciaRepository incidenciaRepository;

	private UsuarioService usuarioService;

	private SeguimientoService seguimientoService;

	private EquipoRepository equipoRepository;

	private ApplicationEventPublisher eventPublisher;

	public IncidenciaService(IncidenciaRepository incidenciaRepository, UsuarioService usuarioService,
			SeguimientoService seguimientoService, EquipoRepository equipoRepository,
			ApplicationEventPublisher eventPublisher) {
		this.incidenciaRepository = incidenciaRepository;
		this.usuarioService = usuarioService;
		this.seguimientoService = seguimientoService;
		this.equipoRepository = equipoRepository;
		this.eventPublisher = eventPublisher;
	}

	/** Publica la notificación; se envía por correo cuando la transacción hace commit. */
	private void notificar(TipoNotificacion tipo, Incidencia incidencia, Usuario autor, Date fecha, String detalle) {
		eventPublisher.publishEvent(NotificacionIncidenciaEvent.de(tipo, incidencia, autor, fecha, detalle));
	}

	/**
	 * Registra fecha/hora y quién cerró al pasar a CERRADO; si se reabre, los limpia.
	 * Devuelve true si la incidencia se acaba de cerrar.
	 */
	private boolean aplicarCierre(Incidencia incidencia, EstadoIncidencia estadoAnterior, Usuario autor, Date fecha) {
		boolean cerrada = incidencia.getEstado() == EstadoIncidencia.CERRADO;
		if (cerrada && estadoAnterior != EstadoIncidencia.CERRADO) {
			incidencia.setFechaCierre(fecha);
			incidencia.setTecnicoCierre(autor);
			return true;
		}
		if (!cerrada) {
			incidencia.setFechaCierre(null);
			incidencia.setTecnicoCierre(null);
		}
		return false;
	}

	/** Busca un usuario activo para usarlo como solicitante. */
	private Usuario buscarSolicitante(Long id) {
		Usuario u = usuarioService.buscarPorId(id)
				.orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado con id: " + id));
		if (u.getEstado() != Estado.ACTIVO) {
			throw new IllegalArgumentException("El solicitante " + u.getNombre() + " está inactivo");
		}
		return u;
	}

	@org.springframework.transaction.annotation.Transactional
	public Incidencia crearIncidencia(IncidenciaDTO dto) {
		Incidencia incidencia = new Incidencia();

		var usuario = usuarioService.obtenerUsuarioSession()
				.orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no encontrado"));

		List<Usuario> tecnicosLibres = usuarioService.encontrarTecnicos("ROLE_TECNICO_NIVEL_1");
		Usuario tecnico = null;
		if (!tecnicosLibres.isEmpty()) {
			// Asigna el primer técnico que no tiene tareas pendientes
			tecnico = tecnicosLibres.get(0);

			if (usuario.getId() == tecnico.getId()) {
				throw new RuntimeException("No pueden ser iguales el usuario con tecnico");
			}
			incidencia.setTecnico(tecnico);
		} else {
			// Si todos están ocupados, puedes dejarlo en null para asignación manual
			// posterior
			incidencia.setTecnico(null);
		}

		// Vincular equipo si se proporcionó
		if (dto.getEquipoId() != null) {
			Equipo equipo = equipoRepository.findById(dto.getEquipoId())
					.orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado con id: " + dto.getEquipoId()));
			incidencia.setEquipo(equipo);
		}

		Date fechaActual = new Date();
		incidencia.setFechaCreacion(fechaActual);
		incidencia.setTitulo(dto.getTitulo());
		incidencia.setDescripcion(dto.getDescripcion());
		incidencia.setUsuario(usuario);
		incidencia.setEstado(EstadoIncidencia.PENDIENTE);
		// Por defecto el solicitante es quien registra la incidencia. Solo el admin puede
		// registrarla a nombre de otro usuario; para el resto se ignora solicitanteId.
		incidencia.setSolicitante(esAdmin(usuario) && dto.getSolicitanteId() != null
				? buscarSolicitante(dto.getSolicitanteId())
				: usuario);

		Incidencia newIncidencia = incidenciaRepository.save(incidencia);

		String equipoInfo = (newIncidencia.getEquipo() != null)
				? " | Equipo afectado: " + newIncidencia.getEquipo().getNombre()
				: "";
		Seguimiento seguimiento = new Seguimiento();
		seguimiento.setIncidencia(newIncidencia);
		seguimiento.setEstado(Estado.ACTIVO);
		seguimiento.setComentario("Incidencia creada por " + usuario.getUsername() + equipoInfo);

		seguimientoService.registrarEventoSistema(seguimiento);

		notificar(TipoNotificacion.CREACION, newIncidencia, usuario, fechaActual,
				"Descripción: " + newIncidencia.getDescripcion());

		return newIncidencia;
	}

	@org.springframework.transaction.annotation.Transactional
	public Incidencia modificarIncidencia(Long id, Incidencia datosNuevos) {
		Optional<Incidencia> encontrada = incidenciaRepository.findById(id);
		if (encontrada.isPresent()) {
			Incidencia i = encontrada.get();
			EstadoIncidencia estadoAnterior = i.getEstado();
			Usuario autor = usuarioService.obtenerUsuarioSession().orElse(null);
			Date ahora = new Date();
			i.setTitulo(datosNuevos.getTitulo());
			i.setDescripcion(datosNuevos.getDescripcion());
			i.setEstado(datosNuevos.getEstado());
			i.setTecnico(datosNuevos.getTecnico());
			// El solicitante llega como { "id": n }; si no llega se conserva el actual.
			if (datosNuevos.getSolicitante() != null && datosNuevos.getSolicitante().getId() != null) {
				i.setSolicitante(buscarSolicitante(datosNuevos.getSolicitante().getId()));
			}
			boolean seCerro = aplicarCierre(i, estadoAnterior, autor, ahora);

			// El equipo llega como { "id": n } o null (sin equipo).
			Equipo equipo = null;
			if (datosNuevos.getEquipo() != null && datosNuevos.getEquipo().getId() != null) {
				Long equipoId = datosNuevos.getEquipo().getId();
				equipo = equipoRepository.findById(equipoId)
						.orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado con id: " + equipoId));
			}
			i.setEquipo(equipo);

			Incidencia actualizada = incidenciaRepository.save(i);

			String usuarioNombre = autor != null ? autor.getUsername().toUpperCase() : "SISTEMA";
			Seguimiento seguimiento = new Seguimiento();
			seguimiento.setIncidencia(actualizada);
			seguimiento.setEstado(Estado.ACTIVO);
			seguimiento.setComentario("Incidencia editada por " + usuarioNombre
					+ (equipo != null ? " | Equipo afectado: " + equipo.getNombre() + " (" + equipo.getCodigo() + ")" : ""));
			seguimientoService.registrarEventoSistema(seguimiento);

			if (seCerro) {
				notificar(TipoNotificacion.CIERRE, actualizada, autor, ahora,
						"La incidencia fue cerrada el " + NotificacionIncidenciaEvent.formatearFecha(ahora) + ".");
			} else {
				String cambioEstado = estadoAnterior != actualizada.getEstado()
						? "Estado: " + estadoAnterior + " → " + actualizada.getEstado() + "\n"
						: "";
				notificar(TipoNotificacion.EDICION, actualizada, autor, ahora,
						cambioEstado + "Se actualizaron los datos de la incidencia.");
			}

			return actualizada;
		}
		throw new IncidenciaNotFoundException("Incidencia no encontrada con id: " + id);
	}

	public List<Incidencia> listarIncidencias() {
		return incidenciaRepository.findAll();
	}

	public Optional<Incidencia> obtenerIncidencia(Long id) {
		return incidenciaRepository.findById(id);
	}

	/**
	 * Detalle de una incidencia para el usuario en sesión: el admin ve todas; el resto solo
	 * aquellas que registró, de las que es solicitante o que tiene asignadas como técnico.
	 */
	public Incidencia obtenerIncidenciaVisible(Long id) {
		Incidencia incidencia = incidenciaRepository.findById(id)
				.orElseThrow(() -> new IncidenciaNotFoundException("Incidencia no encontrada con id: " + id));
		Usuario usuario = usuarioService.obtenerUsuarioSession()
				.orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no encontrado"));
		if (!esAdmin(usuario) && !incidencia.participa(usuario)) {
			throw new AccessDeniedException("No tienes acceso a esta incidencia");
		}
		return incidencia;
	}

	static boolean esAdmin(Usuario usuario) {
		return usuario != null && usuario.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
	}

	public Page<Incidencia> listarIncidenciasPaginado(Pageable pageable) {
		return incidenciaRepository.findAllByOrderByFechaCreacionDesc(pageable);
	}

	public Page<Incidencia> listarIncidenciasPaginadoUsuarioTecnicos(Pageable pageable) {
		return incidenciaRepository.findAllByOrderByFechaCreacionDesc(pageable);
	}

	// Misma transacción = misma instancia por id, así distinct() elimina los repetidos
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public List<Incidencia> misIncidencias() {
		var usuario = usuarioService.obtenerUsuarioSession()
				.orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no encontrado"));
		List<Incidencia> comoUsuario = incidenciaRepository.findByUsuario(usuario);
		List<Incidencia> comoTecnico = incidenciaRepository.findByTecnico(usuario);
		List<Incidencia> comoSolicitante = incidenciaRepository.findBySolicitante(usuario);
		return Stream.of(comoUsuario, comoTecnico, comoSolicitante)
				.flatMap(List::stream)
				.distinct()
				.collect(Collectors.toList());
	}

	public Page<Incidencia> misIncidenciasPage(Pageable pageable) {
		var usuario = usuarioService.obtenerUsuarioSession()
				.orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no encontrado"));
		return incidenciaRepository.findByUsuarioOrTecnico(usuario, pageable);
	}

	public Page<Incidencia> listarIncidenciasXBusqueda(String texto, Pageable pageable) {
		return incidenciaRepository
				.findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrderByFechaCreacionDesc(texto, texto,
						pageable);
	}

	public Page<Incidencia> misIncidenciasBusqueda(String texto, Pageable pageable) {
		var usuario = usuarioService.obtenerUsuarioSession()
				.orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no encontrado"));
		return incidenciaRepository.buscarPorUsuarioOTecnicoYTexto(usuario, usuario, texto, pageable);
	}

	@org.springframework.transaction.annotation.Transactional
	public Incidencia modificarEstado(EstadoIncidenciaRequest est) {
		Incidencia incidencia = incidenciaRepository.findById(est.getIdIncidencia())
				.orElseThrow(() -> new IncidenciaNotFoundException("Incidencia no encontrada con id: " + est.getIdIncidencia()));
		EstadoIncidencia estadoAnterior = incidencia.getEstado();
		Usuario usuarioLogeado = usuarioService.obtenerUsuarioSession().orElse(null);
		boolean isAdmin = esAdmin(usuarioLogeado);
		// Solo el técnico asignado (o el admin) puede cambiar el estado.
		if (!isAdmin && !incidencia.esTecnicoAsignado(usuarioLogeado)) {
			throw new AccessDeniedException("Solo el técnico asignado o el administrador pueden cambiar el estado");
		}
		// Una incidencia cerrada queda bloqueada: solo el administrador puede reabrirla.
		if (estadoAnterior == EstadoIncidencia.CERRADO && !isAdmin) {
			throw new IllegalArgumentException(
					"La incidencia está cerrada. Solo el administrador puede reabrirla o modificarla.");
		}
		Date ahora = new Date();
		incidencia.setEstado(est.getEstado());
		boolean seCerro = aplicarCierre(incidencia, estadoAnterior, usuarioLogeado, ahora);
		Incidencia updateIncidencia = incidenciaRepository.save(incidencia);

		String usuarioNombre = (usuarioLogeado != null) ? usuarioLogeado.getUsername().toUpperCase() : "SISTEMA";

		Seguimiento seguimiento = new Seguimiento();
		seguimiento.setIncidencia(updateIncidencia);
		seguimiento.setEstado(Estado.ACTIVO);
		seguimiento.setComentario("Estado de incidencia actualizado de " + estadoAnterior + " a " + est.getEstado() + " por " + usuarioNombre);

		seguimientoService.registrarEventoSistema(seguimiento);

		if (seCerro) {
			notificar(TipoNotificacion.CIERRE, updateIncidencia, usuarioLogeado, ahora,
					"La incidencia fue cerrada el " + NotificacionIncidenciaEvent.formatearFecha(ahora) + ".");
		} else if (estadoAnterior != updateIncidencia.getEstado()) {
			notificar(TipoNotificacion.CAMBIO_ESTADO, updateIncidencia, usuarioLogeado, ahora,
					"Estado: " + estadoAnterior + " → " + updateIncidencia.getEstado());
		}

		return updateIncidencia;
	}

	public List<TecnicosDTO> listaTecnicoConIncidencia() {
		return incidenciaRepository.obtenerTecnicosConIncidenciasPendientes();
	}

	@org.springframework.transaction.annotation.Transactional
	public Incidencia cambiarTecnico(IncidenciaRequestTecnico dto) {
		Incidencia incidencia = incidenciaRepository.findById(dto.getIdIncidencia())
				.orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada"));

		// regla de negocio: solo permitir cambio si está en estado ABIERTA o PENDIENTE.
		if (incidencia.getEstado().equals(EstadoIncidencia.CERRADO)) {
			throw new IllegalArgumentException("No se puede cambiar técnico en una incidencia cerrada");
		}

		Usuario tecnico = usuarioService.buscarPorId(dto.getIdTecnico())
				.orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado"));
		if (!tecnico.getRoles().stream().anyMatch(r -> r.getName().contains("TECNICO"))) {
			throw new IllegalArgumentException("El usuario no tiene rol de técnico");
		}

		incidencia.setTecnico(tecnico);

		var updateIncidencia = incidenciaRepository.save(incidencia);

		Usuario usuarioLogeado = usuarioService.obtenerUsuarioSession()
        .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no se ha encontrado"));
   

		Seguimiento seguimiento = new Seguimiento();
		seguimiento.setIncidencia(updateIncidencia);
		seguimiento.setEstado(Estado.ACTIVO);
		seguimiento.setComentario("Incidencia modificada por " + usuarioLogeado.getUsername().toUpperCase() + " se asigno al tecnico "+ tecnico.getUsername().toUpperCase());

		seguimientoService.registrarEventoSistema(seguimiento);

		notificar(TipoNotificacion.ASIGNACION, updateIncidencia, usuarioLogeado, new Date(),
				"Técnico responsable: " + tecnico.getNombre());

		return updateIncidencia;
	}
}