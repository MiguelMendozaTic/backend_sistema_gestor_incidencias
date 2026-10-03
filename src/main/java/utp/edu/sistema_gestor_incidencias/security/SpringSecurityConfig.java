package utp.edu.sistema_gestor_incidencias.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import utp.edu.sistema_gestor_incidencias.security.filters.JwtAuthenticationFilter;
import utp.edu.sistema_gestor_incidencias.security.filters.JwtValidationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig {
	
	private AuthenticationConfiguration authenticationConfiguration;

	@Value("${app.cors.allowed-origins:http://localhost:[*],http://127.0.0.1:[*]}")
	private String allowedOrigins;


	public SpringSecurityConfig(AuthenticationConfiguration authenticationConfiguration) {
		this.authenticationConfiguration = authenticationConfiguration;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@SuppressWarnings("null")
	SecurityFilterChain filterChain(HttpSecurity httpSecurity, TokenJwtConfig tokenJwtConfig) throws Exception {

		AuthenticationManager manager = authenticationConfiguration.getAuthenticationManager();

		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(manager, tokenJwtConfig);

		JwtValidationFilter jwtValidationFilter = new JwtValidationFilter(manager, tokenJwtConfig);

		return httpSecurity
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests((authorize) -> authorize
						// Sin registro público: las comprobaciones de usuario/correo libres son del formulario de alta del admin
						.requestMatchers("/api/auth/**").hasRole("ADMIN")

						// Configuración: todos la leen, solo el ADMIN la cambia (el GET va primero).
						.requestMatchers(HttpMethod.GET, "/api/configuracion").authenticated()
						.requestMatchers("/api/configuracion/**", "/api/configuracion").hasRole("ADMIN")

						// Perfil propio: cualquier usuario con sesión (el controlador limita /username a uno mismo o al admin)
						.requestMatchers(HttpMethod.GET,"/api/usuario/*/username").authenticated()
						.requestMatchers(HttpMethod.POST,"/api/usuario/updatePerfil").authenticated()
						// /api/usuario/solicitantes (selector de solicitante) queda solo para ADMIN vía /api/usuario/**:
						// el empleado siempre registra incidencias a su nombre.
						.requestMatchers(HttpMethod.GET, "/api/incidencia/misIncidencias")
						.hasAnyRole("EMPLEADO", "TECNICO_NIVEL_1", "TECNICO_NIVEL_2", "TECNICO_NIVEL_3")
						.requestMatchers(HttpMethod.GET,"/api/incidencia/incidenciasPropias").hasAnyRole("EMPLEADO", "TECNICO_NIVEL_1", "TECNICO_NIVEL_2", "TECNICO_NIVEL_3")
						.requestMatchers(HttpMethod.GET, "/api/incidencia/listarMisIncidenciasXBusqueda").hasAnyRole( "EMPLEADO", "TECNICO_NIVEL_1", "TECNICO_NIVEL_2", "TECNICO_NIVEL_3")
						.requestMatchers(HttpMethod.POST, "/api/incidencia").hasAnyRole("EMPLEADO", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/incidencia/*").hasAnyRole("EMPLEADO", "ADMIN", "TECNICO_NIVEL_1", "TECNICO_NIVEL_2", "TECNICO_NIVEL_3")
						.requestMatchers(HttpMethod.POST, "/api/incidencia/actualizarEstado").hasAnyRole( "ADMIN", "TECNICO_NIVEL_1", "TECNICO_NIVEL_2", "TECNICO_NIVEL_3")
						.requestMatchers(HttpMethod.POST, "/api/incidencia/actualizarTecnico").hasAnyRole( "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/seguimiento/*/seguimientos")
						.hasAnyRole("EMPLEADO", "ADMIN","TECNICO_NIVEL_1", "TECNICO_NIVEL_2", "TECNICO_NIVEL_3")
						.requestMatchers(HttpMethod.POST, "/api/seguimiento")
						.hasAnyRole("EMPLEADO","ADMIN", "TECNICO_NIVEL_1", "TECNICO_NIVEL_2", "TECNICO_NIVEL_3")
						// Equipos: todos los autenticados los consultan (para reportar), solo el ADMIN los gestiona.
						.requestMatchers(HttpMethod.GET, "/api/equipo", "/api/equipo/**").authenticated()
						.requestMatchers("/api/equipo", "/api/equipo/**").hasRole("ADMIN")
						.requestMatchers("/api/dashboard/principal").hasRole("ADMIN")
						.requestMatchers("/api/usuario/**").hasRole("ADMIN")
						.requestMatchers("/api/incidencia/**").hasRole("ADMIN")
						.requestMatchers("/api/seguimiento/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilter(jwtAuthenticationFilter)
				.addFilter(jwtValidationFilter)
				.sessionManagement(manegement -> manegement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration confi = new CorsConfiguration();
		// Solo los orígenes configurados (por defecto el frontend en localhost); antes se aceptaba cualquiera.
		confi.setAllowedOriginPatterns(List.of(allowedOrigins.split("\\s*,\\s*")));
		confi.addAllowedMethod("*");
		confi.addAllowedHeader("*");
		confi.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", confi);

		return source;
	}
}
