package utp.edu.sistema_gestor_incidencias.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.model.Role;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.RoleRepository;
import utp.edu.sistema_gestor_incidencias.repository.UsuarioRepository;
import utp.edu.sistema_gestor_incidencias.security.utils.SecurityUtils;

@Service
public class UsuarioService {

    private UsuarioRepository usuarioRepository;
    private RoleRepository roleRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RoleRepository roleRepository) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
    }

    public Usuario modificarUsuario(Long id, Usuario datosNuevos) {
        Optional<Usuario> encontrado = null;
        if (id == null) {
            encontrado = obtenerUsuarioSession();
        } else {
            encontrado = usuarioRepository.getUserWithRoles(id);
        }

        if (encontrado.isPresent()) {
            Usuario u = encontrado.get();
            validarDatosBasicos(u, datosNuevos);
            // El admin no puede desactivarse a sí mismo (se quedaría sin acceso al sistema).
            if (datosNuevos.getEstado() == Estado.INACTIVO
                    && obtenerUsuarioSession().map(s -> s.getId().equals(u.getId())).orElse(false)) {
                throw new IllegalArgumentException("No puedes desactivar tu propia cuenta.");
            }
            u.setUsername(datosNuevos.getUsername().trim());
            u.setNombre(datosNuevos.getNombre().trim());
            u.setCorreo(datosNuevos.getCorreo().trim());
            u.setArea(datosNuevos.getArea());
            u.setEstado(datosNuevos.getEstado());
            return this.usuarioRepository.save(u);
        }
        return null;
    }

    private static final Pattern USERNAME = Pattern.compile("^\\S{3,20}$");
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Mismas reglas que el alta (UsuarioDTO): username 3-20 sin espacios, nombre 3-35, correo válido,
     * y username/correo únicos (sin contar al propio usuario).
     */
    private void validarDatosBasicos(Usuario actual, Usuario datosNuevos) {
        String username = datosNuevos.getUsername() == null ? "" : datosNuevos.getUsername().trim();
        String nombre = datosNuevos.getNombre() == null ? "" : datosNuevos.getNombre().trim();
        String correo = datosNuevos.getCorreo() == null ? "" : datosNuevos.getCorreo().trim();

        if (!USERNAME.matcher(username).matches())
            throw new IllegalArgumentException("El username debe tener entre 3 y 20 caracteres, sin espacios");
        if (nombre.length() < 3 || nombre.length() > 35)
            throw new IllegalArgumentException("El nombre debe tener entre 3 y 35 caracteres");
        if (!CORREO.matcher(correo).matches())
            throw new IllegalArgumentException("El formato del correo electrónico no es válido");

        usuarioRepository.findByUsername(username)
                .filter(otro -> !otro.getId().equals(actual.getId()))
                .ifPresent(otro -> { throw new IllegalArgumentException("El username ya está en uso."); });
        usuarioRepository.findByCorreo(correo)
                .filter(otro -> !otro.getId().equals(actual.getId()))
                .ifPresent(otro -> { throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado."); });
    }

    public List<Usuario> listarUsuarios() {
        return this.usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerUsuario(Long id) {
        Optional<Usuario> encontrado = this.usuarioRepository.findById(id);
        return encontrado;
    }

    public Optional<Usuario> obtenerUsuarioSession() {
        String username = SecurityUtils.getCurrentUsername();
        Optional<Usuario> encontrado = usuarioRepository.findByUsername(username);
        return encontrado;
    }

    public Page<Usuario> listarPorNombreDescendente(Pageable pageable) {
        return this.usuarioRepository.findAllByOrderByNombreAsc(pageable);
    }

    public Page<Usuario> buscarPorNombreCorreoDesc(String texto, Pageable pageable) {
        return this.usuarioRepository
                .findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(texto, texto, pageable);
    }

    @Transactional
    public Usuario actualizarRole(Long id, String name) {
        Usuario usuarioConRol = usuarioRepository.getUserWithRoles(id)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario no fue encontrado"));

        Optional<Role> optionalRole = roleRepository.findByName("ROLE_" + name);
        optionalRole.ifPresent(usuarioConRol::addRole);

        return usuarioConRol;
    }

    @Transactional
    public Usuario eliminarRol(Long usuarioId, String rolAEliminar) {
        // Buscar usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + usuarioId));

        // Buscar rol
        Role role = roleRepository.findByName(rolAEliminar)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + rolAEliminar));

        // El admin no puede quitarse su propio rol de administrador (se quedaría sin acceso)
        if ("ROLE_ADMIN".equals(role.getName())
                && obtenerUsuarioSession().map(s -> s.getId().equals(usuario.getId())).orElse(false)) {
            throw new IllegalArgumentException("No puedes quitarte tu propio rol de administrador.");
        }

        // Eliminar rol del set
        usuario.getRoles().remove(role);
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> encontrarTecnicos(String name) {
        return usuarioRepository.findTecnicosDisponibles(name);
    }

    public List<Usuario> listarTecnicos() {
        return usuarioRepository.listarTecnicos();
    }

    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        Optional<Usuario> encontrado = usuarioRepository.findByUsername(username);
        return encontrado;
    }

    /**
     * "Mi perfil": el usuario solo cambia su nombre. El usuario (login) y el correo
     * solo los modifica el administrador desde la gestión de usuarios.
     */
    public Usuario actualizarPerfil(Usuario datosNuevos) {
        Optional<Usuario> encontrado = obtenerUsuarioSession();
        if (encontrado.isPresent()) {
            Usuario u = encontrado.get();
            if (cambia(datosNuevos.getUsername(), u.getUsername()) || cambia(datosNuevos.getCorreo(), u.getCorreo())) {
                throw new IllegalArgumentException("El usuario y el correo solo los puede cambiar el administrador.");
            }
            String nombre = datosNuevos.getNombre() == null ? "" : datosNuevos.getNombre().trim();
            if (nombre.length() < 3 || nombre.length() > 35)
                throw new IllegalArgumentException("El nombre debe tener entre 3 y 35 caracteres");
            u.setNombre(nombre);
            return this.usuarioRepository.save(u);
        }
        return null;
    }

    /** true si llega un valor distinto del actual (null = no se envió, no cambia). */
    private static boolean cambia(String nuevo, String actual) {
        return nuevo != null && !nuevo.trim().equals(actual);
    }

    public Optional<Usuario> buscarPorId (Long id){
        return usuarioRepository.findById(id);
    }

    public List<Usuario> buscarSolicitantes(String texto, utp.edu.sistema_gestor_incidencias.enums.Area area) {
        return usuarioRepository.buscarSolicitantes(texto, area,
                org.springframework.data.domain.PageRequest.of(0, 20));
    }
}
