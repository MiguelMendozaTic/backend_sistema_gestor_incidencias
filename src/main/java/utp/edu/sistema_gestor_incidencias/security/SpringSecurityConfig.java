package utp.edu.sistema_gestor_incidencias.security;

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
						.requestMatchers("/api/auth/**").permitAll()

						.requestMatchers(HttpMethod.GET,"/api/usuario/*/username").permitAll()
						.requestMatchers(HttpMethod.POST,"/api/usuario/updatePerfil").permitAll()
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
		confi.addAllowedOriginPattern("*");
		confi.addAllowedMethod("*");
		confi.addAllowedHeader("*");
		confi.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", confi);

		return source;
	}
}
