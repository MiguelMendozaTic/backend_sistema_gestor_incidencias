package utp.edu.sistema_gestor_incidencias.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import utp.edu.sistema_gestor_incidencias.service.EmailService;

@Component
public class CorreoNotificador {

	private static final Logger log = LoggerFactory.getLogger(CorreoNotificador.class);

	private final EmailService emailService;
	private final boolean habilitado;

	public CorreoNotificador(
			EmailService emailService,
			@Value("${notificacion.habilitada:true}") boolean habilitado) {
		this.emailService = emailService;
		this.habilitado = habilitado;
		log.info("[Correo] Notificador configurado con Resend API.");
	}

	@Async
	@TransactionalEventListener(fallbackExecution = true)
	public void enviar(NotificacionIncidenciaEvent evento) {
		if (!habilitado)
			return;

		if (evento.solicitanteCorreo() == null || evento.solicitanteCorreo().isBlank()) {
			log.warn("[Correo] {} sin correo de solicitante; no se envía {}", evento.ticket(), evento.tipo());
			return;
		}

		// Convertimos el texto plano que arma el Evento a HTML para que el correo se
		// vea bonito
		String htmlContent = "<div style='font-family: Arial, sans-serif; font-size: 14px; color: #333;'>"
				+ evento.mensaje().replace("\n", "<br>")
				+ "</div>";

		try {
			// AQUI USAMOS RESEND EN VEZ DE SMTP
			emailService.enviarCorreo(evento.solicitanteCorreo(), evento.asunto(), htmlContent);
			log.info("[Correo] {} de {} enviado a {} vía Resend", evento.tipo(), evento.ticket(),
					evento.solicitanteCorreo());
		} catch (Exception e) {
			log.error("[Correo] No se pudo enviar {} de {} a {}: {}", evento.tipo(), evento.ticket(),
					evento.solicitanteCorreo(), e.getMessage());
		}
	}
}