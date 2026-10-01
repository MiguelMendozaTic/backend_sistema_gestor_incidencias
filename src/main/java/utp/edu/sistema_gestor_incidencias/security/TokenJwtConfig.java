package utp.edu.sistema_gestor_incidencias.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenJwtConfig {
	
	//public static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();

	@Value("${config.jwt.secret}")
	private String SECRET_KEY;

	public static final String PREFIX_TOKEN = "Bearer ";
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String CONTENT_TYPE = "application/json";
  
	public String getSecretKey() {
		return SECRET_KEY;
	}

}
