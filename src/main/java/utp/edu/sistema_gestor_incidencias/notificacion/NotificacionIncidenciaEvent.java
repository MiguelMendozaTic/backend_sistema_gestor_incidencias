package utp.edu.sistema_gestor_incidencias.notificacion;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.model.Usuario;

/**
 * Datos ya resueltos (solo texto) de un cambio en una incidencia.
 * Se arma dentro de la transacción y se envía después del commit,
 * así el envío no depende de entidades JPA ni bloquea la petición.
 */
public record NotificacionIncidenciaEvent(
		TipoNotificacion tipo,
		String ticket,
		String titulo,
		String estado,
		String solicitanteNombre,
		String solicitanteCorreo,
		String tecnico,
		String equipo,
		String realizadoPor,
		String fecha,
		String detalle) {

	public enum TipoNotificacion {
		CREACION("Incidencia registrada"),
		SEGUIMIENTO("Nuevo comentario de seguimiento"),
		CAMBIO_ESTADO("Cambio de estado"),
		ASIGNACION("Técnico asignado"),
		EDICION("Incidencia actualizada"),
		CIERRE("Incidencia cerrada");

		private final String asunto;

		TipoNotificacion(String asunto) {
			this.asunto = asunto;
		}

		public String getAsunto() {
			return asunto;
		}
	}

	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
			.withZone(ZoneId.of("America/Lima"));

	public static String formatearFecha(Date fecha) {
		return fecha == null ? "" : FORMATO.format(fecha.toInstant());
	}

	/** Clave legible igual que en el frontend: INC-0007. */
	public static String ticketKey(Long id) {
		return String.format("INC-%04d", id == null ? 0 : id);
	}

	public static NotificacionIncidenciaEvent de(TipoNotificacion tipo, Incidencia i, Usuario autor, Date fecha,
			String detalle) {
		String equipo = i.getEquipo() != null
				? i.getEquipo().getCodigo() + " · " + i.getEquipo().getNombre()
				: "No especificado";
		String autorNombre = autor != null ? autor.getNombre() + " (@" + autor.getUsername() + ")" : "Sistema";
		// Incidencias anteriores a este cambio no tienen solicitante: se avisa a quien la registró.
		Usuario solicitante = i.getSolicitante() != null ? i.getSolicitante() : i.getUsuario();
		return new NotificacionIncidenciaEvent(
				tipo,
				ticketKey(i.getId()),
				i.getTitulo(),
				i.getEstado() != null ? i.getEstado().name() : "",
				solicitante != null ? solicitante.getNombre() : null,
				solicitante != null ? solicitante.getCorreo() : null,
				i.getTecnico() != null ? i.getTecnico().getNombre() : "Sin asignar",
				equipo,
				autorNombre,
				formatearFecha(fecha),
				detalle);
	}

	public String asunto() {
		return "[" + ticket + "] " + tipo.getAsunto() + ": " + titulo;
	}

	/** Cuerpo en texto plano del correo. */
	public String mensaje() {
		StringBuilder sb = new StringBuilder();
		sb.append("Hola ").append(solicitanteNombre != null ? solicitanteNombre : "").append(",\n\n");
		sb.append(tipo.getAsunto()).append(" en el ticket ").append(ticket).append(".\n\n");
		sb.append("Ticket: ").append(ticket).append('\n');
		sb.append("Resumen: ").append(titulo).append('\n');
		sb.append("Estado actual: ").append(estado).append('\n');
		sb.append("Técnico asignado: ").append(tecnico).append('\n');
		sb.append("Equipo: ").append(equipo).append('\n');
		sb.append("Fecha y hora: ").append(fecha).append('\n');
		sb.append(tipo == TipoNotificacion.CIERRE ? "Cerrado por: " : "Realizado por: ")
				.append(realizadoPor).append('\n');
		if (detalle != null && !detalle.isBlank()) {
			sb.append("\nDetalle:\n").append(detalle).append('\n');
		}
		sb.append("\nDestinatario: ").append(solicitanteCorreo).append('\n');
		sb.append("— Mesa de servicio · Gestor de Incidencias");
		return sb.toString();
	}
}
