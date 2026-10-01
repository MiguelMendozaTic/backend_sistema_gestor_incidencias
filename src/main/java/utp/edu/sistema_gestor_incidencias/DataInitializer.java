package utp.edu.sistema_gestor_incidencias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.TipoEquipo;
import utp.edu.sistema_gestor_incidencias.model.Equipo;
import utp.edu.sistema_gestor_incidencias.repository.EquipoRepository;
import utp.edu.sistema_gestor_incidencias.service.EquipoService;

/**
 * Inicializa la base de datos con equipos de cómputo de muestra
 * al arrancar la aplicación por primera vez (idempotente).
 *
 * Los códigos se asignan de forma determinista:
 *   LAP-001 … LAP-004  → Laptops
 *   PC-001  … PC-006   → Computadoras de escritorio
 *   IMP-001 … IMP-004  → Impresoras
 *   MON-001 … MON-003  → Monitores
 *   SRV-001            → Servidor
 *   PRO-001            → Proyector
 *   SCA-001            → Scanner
 *   OTR-001            → Otro
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EquipoService equipoService;
    private final EquipoRepository equipoRepository;

    public DataInitializer(EquipoService equipoService, EquipoRepository equipoRepository) {
        this.equipoService = equipoService;
        this.equipoRepository = equipoRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (equipoRepository.count() > 0) {
            log.info("[DataInitializer] La tabla equipos ya tiene datos. Se omite la carga inicial.");
            return;
        }

        log.info("[DataInitializer] Cargando equipos iniciales en la base de datos...");

        // ── LAPTOPS ─────────────────────────────────────────────────────────────
        insertar("LAP-001", "Laptop Dell Latitude 5540",
                "Laptop empresarial Intel Core i5-1345U, 16GB RAM, 512GB SSD",
                TipoEquipo.LAPTOP, Area.RRHH);
        insertar("LAP-002", "Laptop HP EliteBook 840 G10",
                "Laptop HP EliteBook Intel Core i7-1360P, 16GB RAM, 512GB SSD",
                TipoEquipo.LAPTOP, Area.ADMINISTRACION);
        insertar("LAP-003", "Laptop Lenovo ThinkPad E14",
                "Laptop Lenovo AMD Ryzen 5 7530U, 8GB RAM, 256GB SSD",
                TipoEquipo.LAPTOP, Area.CONTABILIDAD);
        insertar("LAP-004", "Laptop Asus VivoBook 15",
                "Laptop Asus Intel Core i5-12500H, 8GB RAM, 512GB SSD",
                TipoEquipo.LAPTOP, Area.LOGISTICA);

        // ── PCs DE ESCRITORIO ────────────────────────────────────────────────────
        insertar("PC-001", "PC Dell OptiPlex 7010",
                "PC escritorio Intel Core i7-12700, 16GB RAM, 1TB HDD + 256GB SSD",
                TipoEquipo.PC, Area.SISTEMAS);
        insertar("PC-002", "PC HP ProDesk 400 G9",
                "PC escritorio Intel Core i5-12500, 8GB RAM, 512GB SSD",
                TipoEquipo.PC, Area.ADMINISTRACION);
        insertar("PC-003", "PC Lenovo ThinkCentre M70q",
                "Mini PC Intel Core i5-12400T, 8GB RAM, 256GB SSD",
                TipoEquipo.PC, Area.RRHH);
        insertar("PC-004", "PC Asus ExpertCenter D700ME",
                "PC escritorio Intel Core i7-12700, 32GB RAM, 1TB SSD, RTX 3060",
                TipoEquipo.PC, Area.SISTEMAS);
        insertar("PC-005", "PC Dell Vostro 3910",
                "PC escritorio Intel Core i3-12100, 8GB RAM, 256GB SSD",
                TipoEquipo.PC, Area.CONTABILIDAD);
        insertar("PC-006", "PC HP Slim Desktop S01",
                "PC compacto Intel Core i5-12400, 8GB RAM, 512GB SSD",
                TipoEquipo.PC, Area.GERENCIA);

        // ── IMPRESORAS ───────────────────────────────────────────────────────────
        insertar("IMP-001", "Impresora HP LaserJet Pro M404dn",
                "Impresora láser monocromo, 40 ppm, dúplex automático, red ethernet",
                TipoEquipo.IMPRESORA, Area.ADMINISTRACION);
        insertar("IMP-002", "Impresora Epson EcoTank L3250",
                "Impresora multifunción inkjet color, WiFi, sin cartuchos",
                TipoEquipo.IMPRESORA, Area.RRHH);
        insertar("IMP-003", "Impresora Canon PIXMA G3160",
                "Impresora multifunción a color con sistema de tinta continua, WiFi",
                TipoEquipo.IMPRESORA, Area.CONTABILIDAD);
        insertar("IMP-004", "Impresora Brother HL-L8360CDW",
                "Impresora láser color, 33 ppm, dúplex, WiFi, NFC",
                TipoEquipo.IMPRESORA, Area.GERENCIA);

        // ── MONITORES ────────────────────────────────────────────────────────────
        insertar("MON-001", "Monitor Dell UltraSharp U2722D",
                "Monitor 27\" IPS 4K UHD, USB-C 90W, DisplayPort, HDMI",
                TipoEquipo.MONITOR, Area.SISTEMAS);
        insertar("MON-002", "Monitor LG 24MP60G-B",
                "Monitor 24\" IPS FHD 75Hz, AMD FreeSync, HDMI, DisplayPort",
                TipoEquipo.MONITOR, Area.ADMINISTRACION);
        insertar("MON-003", "Monitor Samsung 32\" S32B700",
                "Monitor 32\" QHD 60Hz, USB-C, HDR10, Eye Saver",
                TipoEquipo.MONITOR, Area.GERENCIA);

        // ── SERVIDOR ─────────────────────────────────────────────────────────────
        insertar("SRV-001", "Servidor Dell PowerEdge R550",
                "Servidor rack 2U, Intel Xeon Silver 4310, 64GB RAM ECC, 4x 1TB SAS",
                TipoEquipo.SERVIDOR, Area.SISTEMAS);

        // ── PROYECTOR ────────────────────────────────────────────────────────────
        insertar("PRO-001", "Proyector Epson PowerLite 2247U",
                "Proyector WUXGA 4200 lúmenes, WiFi, Miracast, HDMI x2",
                TipoEquipo.PROYECTOR, Area.GERENCIA);

        // ── SCANNER ──────────────────────────────────────────────────────────────
        insertar("SCA-001", "Scanner Fujitsu ScanSnap iX1600",
                "Escáner de documentos dúplex ADF 40 ppm, WiFi, USB, color",
                TipoEquipo.SCANNER, Area.ADMINISTRACION);

        // ── OTRO ─────────────────────────────────────────────────────────────────
        insertar("OTR-001", "UPS APC Back-UPS 1000VA",
                "Sistema de alimentación ininterrumpida 1000VA/600W, 8 tomas, USB",
                TipoEquipo.OTRO, Area.SISTEMAS);

        log.info("[DataInitializer] ✅ {} equipos registrados exitosamente.", equipoRepository.count());
    }

    private void insertar(String codigo, String nombre, String descripcion,
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
