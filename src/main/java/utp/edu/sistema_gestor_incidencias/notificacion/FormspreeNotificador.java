package utp.edu.sistema_gestor_incidencias.notificacion;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía las notificaciones de incidencias a Formspree.
 *
 * Formspree entrega cada envío al correo dueño del formulario. El correo del
 * solicitante va en el campo "email": Formspree lo usa como "Responder a" y,
 * si el formulario tiene activada la autorespuesta, le envía la copia.
 *
 * Se ejecuta después del commit y de forma asíncrona: si Formspree falla, la
 * incidencia igual queda guardada y solo se registra el error en el log.
 */
@Component
public class FormspreeNotificador {

	private static final Logger log = LoggerFactory.getLogger(FormspreeNotificador.class);

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private final String endpoint;
	private final boolean habilitado;

	public FormspreeNotificador(
			@Value("${notificacion.formspree.url:}") String endpoint,
			@Value("${notificacion.formspree.habilitada:false}") boolean habilitado) {
		this.endpoint = endpoint;
		this.habilitado = habilitado;
	}

	@TransactionalEventListener(fallbackExecution = true)
	public void enviar(NotificacionIncidenciaEvent evento) {
		if (!habilitado || endpoint.isBlank()) {
			log.debug("[Notificación] Deshabilitada; se omite {} de {}", evento.tipo(), evento.ticket());
			return;
		}
		if (evento.solicitanteCorreo() == null || evento.solicitanteCorreo().isBlank()) {
			log.warn("[Notificación] {} sin correo de solicitante; no se envía {}", evento.ticket(), evento.tipo());
			return;
		}

		Map<String, String> campos = new LinkedHashMap<>();
		campos.put("email", evento.solicitanteCorreo());
		campos.put("_subject", evento.asunto());
		campos.put("message", evento.mensaje());
		campos.put("ticket", evento.ticket());
		campos.put("evento", evento.tipo().getAsunto());
		campos.put("estado", evento.estado());
		campos.put("solicitante", evento.solicitanteNombre());
		campos.put("tecnico", evento.tecnico());
		campos.put("equipo", evento.equipo());
		campos.put("realizado_por", evento.realizadoPor());
		campos.put("fecha_hora", evento.fecha());

		String cuerpo = campos.entrySet().stream()
				.map(e -> codificar(e.getKey()) + "=" + codificar(e.getValue()))
				.collect(Collectors.joining("&"));

		HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
				.timeout(Duration.ofSeconds(15))
				.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8))
				.build();

		httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.whenComplete((resp, error) -> {
					if (error != null) {
						log.error("[Notificación] No se pudo enviar {} de {}: {}", evento.tipo(), evento.ticket(),
								error.getMessage());
					} else if (resp.statusCode() >= 300) {
						log.error("[Notificación] Formspree respondió {} para {} de {}: {}", resp.statusCode(),
								evento.tipo(), evento.ticket(), resp.body());
					} else {
						log.info("[Notificación] {} de {} enviada a {}", evento.tipo(), evento.ticket(),
								evento.solicitanteCorreo());
					}
				});
	}

	private static String codificar(String valor) {
		return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
	}
}
