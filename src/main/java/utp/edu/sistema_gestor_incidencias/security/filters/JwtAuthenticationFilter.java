package utp.edu.sistema_gestor_incidencias.security.filters;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;   // ← CAMBIO 1

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utp.edu.sistema_gestor_incidencias.dto.login.LoginRequestDTO;
import utp.edu.sistema_gestor_incidencias.security.TokenJwtConfig;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;   // ← CAMBIO 2: final
    private final TokenJwtConfig tokenConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();   // ← CAMBIO 3: reutilizar

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, TokenJwtConfig tokenConfig) {
        this.authenticationManager = authenticationManager;
        this.tokenConfig = tokenConfig;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        LoginRequestDTO login;
        try {
            login = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);
        } catch (Exception e) {
            // ← CAMBIO 4: no devolver null, lanzar excepción de auth
            throw new AuthenticationException("Error al parsear el cuerpo del login: " + e.getMessage()) {};
        }

        if (login.getUsername() == null || login.getPassword() == null) {
            throw new AuthenticationException("Username y password son obligatorios") {};
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword());

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
	@SuppressWarnings("null")
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        User user = (User) authResult.getPrincipal();
        String username = user.getUsername();

        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();
        List<String> authorities = roles.stream()
                .filter(Objects::nonNull)                      
                .map(GrantedAuthority::getAuthority)
                .toList();

        Claims claims = Jwts.claims().add("authorities", authorities).build();

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(tokenConfig.getSecretKey()));

        String token = Jwts.builder()
                .subject(username)
                .claims(claims)
                .signWith(key)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .compact();

        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("message", String.format("%s ha iniciado sesión con éxito", username));

        // ← CAMBIO 5: primero content-type, luego escribir
        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setStatus(200);
        response.addHeader(TokenJwtConfig.HEADER_AUTHORIZATION, TokenJwtConfig.PREFIX_TOKEN + token);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {
        Map<String, String> body = new HashMap<>();
        // Mensaje genérico: no revelar si el usuario existe o si la cuenta está inactiva.
        body.put("message", "Error en la autenticación, username o password incorrectos");

        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setStatus(401);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}