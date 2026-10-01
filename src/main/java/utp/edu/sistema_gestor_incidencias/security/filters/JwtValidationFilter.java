package utp.edu.sistema_gestor_incidencias.security.filters;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;
import utp.edu.sistema_gestor_incidencias.security.TokenJwtConfig;

import static utp.edu.sistema_gestor_incidencias.security.TokenJwtConfig.*;

public class JwtValidationFilter extends BasicAuthenticationFilter{

	private TokenJwtConfig tokenConfig;

	public JwtValidationFilter(AuthenticationManager authenticationManager, TokenJwtConfig tokenConfig) {
		super(authenticationManager);
		this.tokenConfig = tokenConfig;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String header = request.getHeader(HEADER_AUTHORIZATION);
		if(header == null || !header.startsWith(PREFIX_TOKEN)) {
			chain.doFilter(request, response);
			return;
		}
		
		String token  = header.replace(PREFIX_TOKEN, "");
		SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(tokenConfig.getSecretKey()));

		try {
			Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
			String username =  claims.getSubject();
			
			List<String> authoritiesClaims = claims.get("authorities", List.class);
			
			Collection<? extends GrantedAuthority> authorities = authoritiesClaims.stream()
					.map(SimpleGrantedAuthority::new)
					.toList();
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					username, null, authorities
					);
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			chain.doFilter(request, response);
		} catch (JwtException e) {
			Map<String , String > body = new HashMap<>();
			body.put("message", "El token JWT es invalido");
			body.put("error", e.getMessage());
			response.getWriter().write(new ObjectMapper().writeValueAsString(body));
			response.setContentType(CONTENT_TYPE);
			response.setStatus(401);
		}
		
	}
	
}
