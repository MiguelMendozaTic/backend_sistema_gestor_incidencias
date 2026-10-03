package utp.edu.sistema_gestor_incidencias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // envío de correos en segundo plano
public class SistemaGestorIncidenciasApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaGestorIncidenciasApplication.class, args);
	}

}
