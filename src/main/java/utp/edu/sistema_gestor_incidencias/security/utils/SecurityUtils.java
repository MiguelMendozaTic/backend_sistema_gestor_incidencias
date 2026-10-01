package utp.edu.sistema_gestor_incidencias.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
	private SecurityUtils() {}
	
	public static String getCurrentUsername() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth ==null || !auth.isAuthenticated()) {
			throw new IllegalStateException("No hay ningun usuario autenticado");
		}
		
		return auth.getName();
	}
}