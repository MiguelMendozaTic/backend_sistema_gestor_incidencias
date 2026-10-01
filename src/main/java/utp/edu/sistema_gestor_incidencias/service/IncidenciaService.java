package utp.edu.sistema_gestor_incidencias.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

@Service
public class IncidenciaService {

	private IncidenciaRepository incidenciaRepository;

	private UsuarioService usuarioService;

	private SeguimientoService seguimientoService;

	private EquipoRepository equipoRepository;

	public IncidenciaService(IncidenciaRepository incidenciaRepository, UsuarioService usuarioService,
			SeguimientoService seguimientoService, EquipoRepository equipoRepository) {
		this.incidenciaRepository = incidenciaRepository;
		this.usuarioService = usuarioService;
		this.seguimientoService = seguimientoService;
		this.equipoRepository = equipoRepository;
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

		Incidencia newIncidencia = incidenciaRepository.save(incidencia);

		String equipoInfo = (newIncidencia.getEquipo() != null)
				? " | Equipo afectado: " + newIncidencia.getEquipo().getNombre()
				: "";
		Seguimiento seguimiento = new Seguimiento();
		seguimiento.setIncidencia(newIncidencia);
		seguimiento.setEstado(Estado.ACTIVO);
		seguimiento.setComentario("Incidencia creada por " + usuario.getUsername() + equipoInfo);

		seguimientoService.crearSeguimiento(seguimiento);

		return newIncidencia;
	}

	public Incidencia modificarIncidencia(Long id, Incidencia datosNuevos) {
		Optional<Incidencia> encontrada = incidenciaRepository.findById(id);
		if (encontrada.isPresent()) {
			Incidencia i = encontrada.get();
			i.setTitulo(datosNuevos.getTitulo());
			i.setDescripcion(datosNuevos.getDescripcion());
			i.setEstado(datosNuevos.getEstado());
			i.setTecnico(datosNuevos.getTecnico());
			return incidenciaRepository.save(i);
		}
		throw new IncidenciaNotFoundException("Incidencia no encontrada con id: " + id);
	}

	public List<Incidencia> listarIncidencias() {
		return incidenciaRepository.findAll();
	}

	public Optional<Incidencia> obtenerIncidencia(Long id) {
		return incidenciaRepository.findById(id);
	}

	public Page<Incidencia> listarIncidenciasPaginado(Pageable pageable) {
		return incidenciaRepository.findAllByOrderByFechaCreacionDesc(pageable);
	}

	public Page<Incidencia> listarIncidenciasPaginadoUsuarioTecnicos(Pageable pageable) {
		return incidenciaRepository.findAllByOrderByFechaCreacionDesc(pageable);
	}

	public List<Incidencia> misIncidencias() {
		var usuario = usuarioService.obtenerUsuarioSession()
				.orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no encontrado"));
		List<Incidencia> comoUsuario = incidenciaRepository.findByUsuario(usuario);
		List<Incidencia> comoTecnico = incidenciaRepository.findByTecnico(usuario);
		return Stream.concat(comoUsuario.stream(), comoTecnico.stream())
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
		incidencia.setEstado(est.getEstado());
		Incidencia updateIncidencia = incidenciaRepository.save(incidencia);

		Usuario usuarioLogeado = usuarioService.obtenerUsuarioSession().orElse(null);
		String usuarioNombre = (usuarioLogeado != null) ? usuarioLogeado.getUsername().toUpperCase() : "SISTEMA";

		Seguimiento seguimiento = new Seguimiento();
		seguimiento.setIncidencia(updateIncidencia);
		seguimiento.setEstado(Estado.ACTIVO);
		seguimiento.setComentario("Estado de incidencia actualizado de " + estadoAnterior + " a " + est.getEstado() + " por " + usuarioNombre);

		seguimientoService.crearSeguimiento(seguimiento);

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

		seguimientoService.crearSeguimiento(seguimiento);

		return updateIncidencia;
	}
}