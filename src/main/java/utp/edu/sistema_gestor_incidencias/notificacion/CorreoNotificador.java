package utp.edu.sistema_gestor_incidencias.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía la notificación directamente al correo del solicitante por SMTP.
 *
 * Formspree solo entrega al dueño del formulario; este notificador es el que
 * hace llegar el aviso al solicitante. Si no hay credenciales SMTP
 * (MAIL_USERNAME / MAIL_PASSWORD en el .env) no envía nada y lo avisa en el log.
 */
@Component
public class CorreoNotificador {

	private static final Logger log = LoggerFactory.getLogger(CorreoNotificador.class);

	private final ObjectProvider<JavaMailSender> mailSender;
	private final String usuarioSmtp;
	private final String remitente;
	private final boolean habilitado;

	public CorreoNotificador(
			ObjectProvider<JavaMailSender> mailSender,
			@Value("${spring.mail.username:}") String usuarioSmtp,
			@Value("${notificacion.correo.remitente:}") String remitente,
			@Value("${notificacion.habilitada:true}") boolean habilitado) {
		this.mailSender = mailSender;
		this.usuarioSmtp = usuarioSmtp;
		this.remitente = remitente.isBlank() ? usuarioSmtp : remitente;
		this.habilitado = habilitado;
		if (usuarioSmtp.isBlank()) {
			log.warn("[Correo] SMTP sin configurar: define MAIL_USERNAME y MAIL_PASSWORD en el .env del backend");
		} else {
			log.info("[Correo] SMTP configurado; los avisos se enviarán desde {}", this.remitente);
		}
	}

	@Async
	@TransactionalEventListener(fallbackExecution = true)
	public void enviar(NotificacionIncidenciaEvent evento) {
		if (!habilitado) return;
		JavaMailSender sender = mailSender.getIfAvailable();
		if (sender == null || usuarioSmtp.isBlank()) {
			log.warn("[Correo] SMTP sin configurar (MAIL_USERNAME/MAIL_PASSWORD); no se envió {} de {} a {}",
					evento.tipo(), evento.ticket(), evento.solicitanteCorreo());
			return;
		}
		if (evento.solicitanteCorreo() == null || evento.solicitanteCorreo().isBlank()) {
			log.warn("[Correo] {} sin correo de solicitante; no se envía {}", evento.ticket(), evento.tipo());
			return;
		}

		SimpleMailMessage correo = new SimpleMailMessage();
		correo.setFrom(remitente);
		correo.setTo(evento.solicitanteCorreo());
		correo.setSubject(evento.asunto());
		correo.setText(evento.mensaje());
		try {
			sender.send(correo);
			log.info("[Correo] {} de {} enviado a {}", evento.tipo(), evento.ticket(), evento.solicitanteCorreo());
		} catch (MailException e) {
			log.error("[Correo] No se pudo enviar {} de {} a {}: {}", evento.tipo(), evento.ticket(),
					evento.solicitanteCorreo(), e.getMessage());
		}
	}
}
