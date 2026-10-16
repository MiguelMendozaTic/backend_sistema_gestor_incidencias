package utp.edu.sistema_gestor_incidencias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.enums.TipoEquipo;
import utp.edu.sistema_gestor_incidencias.model.ConfiguracionSistema;
import utp.edu.sistema_gestor_incidencias.model.Equipo;
import utp.edu.sistema_gestor_incidencias.model.Role;
import utp.edu.sistema_gestor_incidencias.model.Usuario;
import utp.edu.sistema_gestor_incidencias.repository.ConfiguracionRepository;
import utp.edu.sistema_gestor_incidencias.repository.EquipoRepository;
import utp.edu.sistema_gestor_incidencias.repository.RoleRepository;
import utp.edu.sistema_gestor_incidencias.repository.UsuarioRepository;
import utp.edu.sistema_gestor_incidencias.service.EquipoService;

import java.util.List;

/**
 * Inicializa la base de datos con los datos minimos requeridos al arrancar la
 * aplicacion (roles, usuarios de prueba y configuracion del sistema).
 * Cada bloque comprueba si el dato ya existe antes de insertarlo (idempotente).
 *
 * NOTA: los roles se buscan desde el repositorio para que esten en el contexto
 * JPA activo al momento de asociarlos al usuario, evitando "detached entity".
 */
@Configuration
public class DataInitializer {

        private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

        @Bean
        CommandLineRunner initDatabase(
                        RoleRepository roleRepo,
                        UsuarioRepository usuarioRepo,
                        ConfiguracionRepository configuracionRepo,
                        PasswordEncoder passwordEncoder,
                        EquipoRepository equipoRepository,
                        EquipoService equipoService) {

                return args -> {

                        // 1. Roles del sistema
                        List<String> nombresRoles = List.of(
                                        "ROLE_ADMIN",
                                        "ROLE_EMPLEADO",
                                        "ROLE_TECNICO_NIVEL_1",
                                        "ROLE_TECNICO_NIVEL_2",
                                        "ROLE_TECNICO_NIVEL_3");

                        for (String nombreRol : nombresRoles) {
                                if (roleRepo.findByName(nombreRol).isEmpty()) {
                                        roleRepo.save(new Role(null, nombreRol));
                                        log.info("Rol creado: {}", nombreRol);
                                }
                        }

                        // IMPORTANTE: buscamos los roles DESPUES de haberlos guardado,
                        // para que los objetos obtenidos esten gestionados por el contexto
                        // JPA actual y no causen "detached entity" al asociarlos a usuarios.

                        // 2. Usuario administrador
                        if (!usuarioRepo.existsByUsername("admin")) {
                                Role rolAdmin = roleRepo.findByName("ROLE_ADMIN").get();
                                Usuario admin = new Usuario();
                                admin.setUsername("admin");
                                admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
                                admin.setNombre("Administrador del Sistema");
                                admin.setCorreo("admin@sistema.com");
                                admin.setEstado(Estado.ACTIVO);
                                admin.setArea(Area.SISTEMAS);
                                admin.addRole(rolAdmin);
                                usuarioRepo.save(admin);
                                log.info("Usuario admin creado: admin / Admin123!");
                        }

                        // 3. Usuario tecnico nivel 1
                        if (!usuarioRepo.existsByUsername("tecnico1")) {
                                Role rolTec1 = roleRepo.findByName("ROLE_TECNICO_NIVEL_1").get();
                                Usuario tecnico = new Usuario();
                                tecnico.setUsername("tecnico1");
                                tecnico.setPasswordHash(passwordEncoder.encode("Tecnico123!"));
                                tecnico.setNombre("Tecnico Nivel 1");
                                tecnico.setCorreo("tecnico1@sistema.com");
                                tecnico.setEstado(Estado.ACTIVO);
                                tecnico.setArea(Area.SISTEMAS);
                                tecnico.addRole(rolTec1);
                                usuarioRepo.save(tecnico);
                                log.info("Usuario tecnico1 creado: tecnico1 / Tecnico123!");
                        }

                        // 4. Usuario empleado
                        if (!usuarioRepo.existsByUsername("empleado1")) {
                                Role rolEmp = roleRepo.findByName("ROLE_EMPLEADO").get();
                                Usuario empleado = new Usuario();
                                empleado.setUsername("empleado1");
                                empleado.setPasswordHash(passwordEncoder.encode("Empleado123!"));
                                empleado.setNombre("Empleado de Prueba");
                                empleado.setCorreo("empleado1@sistema.com");
                                empleado.setEstado(Estado.ACTIVO);
                                empleado.setArea(Area.RRHH);
                                empleado.addRole(rolEmp);
                                usuarioRepo.save(empleado);
                                log.info("Usuario empleado1 creado: empleado1 / Empleado123!");
                        }

                        // 5. Configuracion del sistema (fila unica id=1)
                        if (configuracionRepo.findById(ConfiguracionSistema.ID).isEmpty()) {
                                configuracionRepo.save(new ConfiguracionSistema());
                                log.info("ConfiguracionSistema creada con intervaloRefresco=30s");
                        }

                        if (equipoRepository.count() > 0) {
                                log.info("[DataInitializer] La tabla equipos ya tiene datos. Se omite la carga inicial.");
                                return;
                        }

                        log.info("[DataInitializer] Cargando equipos iniciales en la base de datos...");

                        // ── LAPTOPS ─────────────────────────────────────────────────────────────
                        insertar(equipoService, "LAP-001", "Laptop Dell Latitude 5540",
                                        "Laptop empresarial Intel Core i5-1345U, 16GB RAM, 512GB SSD",
                                        TipoEquipo.LAPTOP, Area.RRHH);
                        insertar(equipoService, "LAP-002", "Laptop HP EliteBook 840 G10",
                                        "Laptop HP EliteBook Intel Core i7-1360P, 16GB RAM, 512GB SSD",
                                        TipoEquipo.LAPTOP, Area.ADMINISTRACION);
                        insertar(equipoService, "LAP-003", "Laptop Lenovo ThinkPad E14",
                                        "Laptop Lenovo AMD Ryzen 5 7530U, 8GB RAM, 256GB SSD",
                                        TipoEquipo.LAPTOP, Area.CONTABILIDAD);
                        insertar(equipoService, "LAP-004", "Laptop Asus VivoBook 15",
                                        "Laptop Asus Intel Core i5-12500H, 8GB RAM, 512GB SSD",
                                        TipoEquipo.LAPTOP, Area.LOGISTICA);

                        // ── PCs DE ESCRITORIO ────────────────────────────────────────────────────
                        insertar(equipoService, "PC-001", "PC Dell OptiPlex 7010",
                                        "PC escritorio Intel Core i7-12700, 16GB RAM, 1TB HDD + 256GB SSD",
                                        TipoEquipo.PC, Area.SISTEMAS);
                        insertar(equipoService, "PC-002", "PC HP ProDesk 400 G9",
                                        "PC escritorio Intel Core i5-12500, 8GB RAM, 512GB SSD",
                                        TipoEquipo.PC, Area.ADMINISTRACION);
                        insertar(equipoService, "PC-003", "PC Lenovo ThinkCentre M70q",
                                        "Mini PC Intel Core i5-12400T, 8GB RAM, 256GB SSD",
                                        TipoEquipo.PC, Area.RRHH);
                        insertar(equipoService, "PC-004", "PC Asus ExpertCenter D700ME",
                                        "PC escritorio Intel Core i7-12700, 32GB RAM, 1TB SSD, RTX 3060",
                                        TipoEquipo.PC, Area.SISTEMAS);
                        insertar(equipoService, "PC-005", "PC Dell Vostro 3910",
                                        "PC escritorio Intel Core i3-12100, 8GB RAM, 256GB SSD",
                                        TipoEquipo.PC, Area.CONTABILIDAD);
                        insertar(equipoService, "PC-006", "PC HP Slim Desktop S01",
                                        "PC compacto Intel Core i5-12400, 8GB RAM, 512GB SSD",
                                        TipoEquipo.PC, Area.GERENCIA);

                        // ── IMPRESORAS ───────────────────────────────────────────────────────────
                        insertar(equipoService, "IMP-001", "Impresora HP LaserJet Pro M404dn",
                                        "Impresora láser monocromo, 40 ppm, dúplex automático, red ethernet",
                                        TipoEquipo.IMPRESORA, Area.ADMINISTRACION);
                        insertar(equipoService, "IMP-002", "Impresora Epson EcoTank L3250",
                                        "Impresora multifunción inkjet color, WiFi, sin cartuchos",
                                        TipoEquipo.IMPRESORA, Area.RRHH);
                        insertar(equipoService, "IMP-003", "Impresora Canon PIXMA G3160",
                                        "Impresora multifunción a color con sistema de tinta continua, WiFi",
                                        TipoEquipo.IMPRESORA, Area.CONTABILIDAD);
                        insertar(equipoService, "IMP-004", "Impresora Brother HL-L8360CDW",
                                        "Impresora láser color, 33 ppm, dúplex, WiFi, NFC",
                                        TipoEquipo.IMPRESORA, Area.GERENCIA);

                        // ── MONITORES ────────────────────────────────────────────────────────────
                        insertar(equipoService, "MON-001", "Monitor Dell UltraSharp U2722D",
                                        "Monitor 27\" IPS 4K UHD, USB-C 90W, DisplayPort, HDMI",
                                        TipoEquipo.MONITOR, Area.SISTEMAS);
                        insertar(equipoService, "MON-002", "Monitor LG 24MP60G-B",
                                        "Monitor 24\" IPS FHD 75Hz, AMD FreeSync, HDMI, DisplayPort",
                                        TipoEquipo.MONITOR, Area.ADMINISTRACION);
                        insertar(equipoService, "MON-003", "Monitor Samsung 32\" S32B700",
                                        "Monitor 32\" QHD 60Hz, USB-C, HDR10, Eye Saver",
                                        TipoEquipo.MONITOR, Area.GERENCIA);

                        // ── SERVIDOR ─────────────────────────────────────────────────────────────
                        insertar(equipoService, "SRV-001", "Servidor Dell PowerEdge R550",
                                        "Servidor rack 2U, Intel Xeon Silver 4310, 64GB RAM ECC, 4x 1TB SAS",
                                        TipoEquipo.SERVIDOR, Area.SISTEMAS);

                        // ── PROYECTOR ────────────────────────────────────────────────────────────
                        insertar(equipoService, "PRO-001", "Proyector Epson PowerLite 2247U",
                                        "Proyector WUXGA 4200 lúmenes, WiFi, Miracast, HDMI x2",
                                        TipoEquipo.PROYECTOR, Area.GERENCIA);

                        // ── SCANNER ──────────────────────────────────────────────────────────────
                        insertar(equipoService, "SCA-001", "Scanner Fujitsu ScanSnap iX1600",
                                        "Escáner de documentos dúplex ADF 40 ppm, WiFi, USB, color",
                                        TipoEquipo.SCANNER, Area.ADMINISTRACION);

                        // ── OTRO ─────────────────────────────────────────────────────────────────
                        insertar(equipoService, "OTR-001", "UPS APC Back-UPS 1000VA",
                                        "Sistema de alimentación ininterrumpida 1000VA/600W, 8 tomas, USB",
                                        TipoEquipo.OTRO, Area.SISTEMAS);

                        log.info("=== DataInitializer completado ===");
                };
        }

        private void insertar(EquipoService equipoService, String codigo, String nombre, String descripcion,
                        TipoEquipo tipo, Area area) {
                Equipo e = new Equipo();
                e.setCodigo(codigo);
                e.setNombre(nombre);
                e.setDescripcion(descripcion);
                e.setTipo(tipo);
                e.setArea(area);
                Equipo creado = equipoService.crearEquipoConCodigo(e);
                if (creado != null) {
                        log.info("  [+] {} → {} ({})", creado.getCodigo(), creado.getNombre(), creado.getTipo());
                }

        }
}
