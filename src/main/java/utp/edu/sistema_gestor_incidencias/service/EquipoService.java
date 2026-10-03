package utp.edu.sistema_gestor_incidencias.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.enums.TipoEquipo;
import utp.edu.sistema_gestor_incidencias.exception.IncidenciaNotFoundException;
import utp.edu.sistema_gestor_incidencias.model.Equipo;
import utp.edu.sistema_gestor_incidencias.repository.EquipoRepository;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;

    public EquipoService(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    /**
     * Genera el próximo código correlativo para un tipo de equipo.
     * Formato: {PREFIJO}-{NUMERO_3_DIGITOS}  →  LAP-001, PC-003, IMP-012, etc.
     */
    public String generarCodigo(TipoEquipo tipo) {
        String prefijo = tipo.getPrefijo();
        List<String> codigos = equipoRepository.findCodigosByPrefijo(prefijo);

        int siguiente = 1;
        if (!codigos.isEmpty()) {
            // El primer resultado es el código más alto (ORDER BY DESC)
            String ultimoCodigo = codigos.get(0);
            try {
                String[] partes = ultimoCodigo.split("-");
                int ultimoNumero = Integer.parseInt(partes[partes.length - 1]);
                siguiente = ultimoNumero + 1;
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                siguiente = codigos.size() + 1;
            }
        }

        return String.format("%s-%03d", prefijo, siguiente);
    }

    /**
     * Crea un equipo. Si no viene con código, lo genera automáticamente.
     * Si viene con código, valida que no exista duplicado.
     */
    public Equipo crearEquipo(Equipo equipo) {
        if (equipo.getCodigo() == null || equipo.getCodigo().isBlank()) {
            equipo.setCodigo(generarCodigo(equipo.getTipo()));
        } else if (equipoRepository.existsByCodigo(equipo.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un equipo con el código: " + equipo.getCodigo());
        }
        equipo.setEstado(Estado.ACTIVO);
        return equipoRepository.save(equipo);
    }

    /**
     * Crea un equipo con código manual explícito (para el DataInitializer).
     * Omite validación de duplicado — verifica antes de llamar.
     */
    public Equipo crearEquipoConCodigo(Equipo equipo) {
        if (equipoRepository.existsByCodigo(equipo.getCodigo())) {
            return null; // ya existe, no insertar
        }
        equipo.setEstado(Estado.ACTIVO);
        return equipoRepository.save(equipo);
    }

    public Equipo modificarEquipo(Long id, Equipo datos) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new IncidenciaNotFoundException("Equipo no encontrado con id: " + id));
        if (datos.getCodigo() != null && !datos.getCodigo().isBlank()) {
            if (equipoRepository.existsByCodigoAndIdNot(datos.getCodigo(), id)) {
                throw new IllegalArgumentException("Ya existe un equipo con el código: " + datos.getCodigo());
            }
            equipo.setCodigo(datos.getCodigo());
        }
        equipo.setNombre(datos.getNombre());
        equipo.setDescripcion(datos.getDescripcion());
        equipo.setTipo(datos.getTipo());
        equipo.setArea(datos.getArea());
        if (datos.getEstado() != null) {
            equipo.setEstado(datos.getEstado());
        }
        return equipoRepository.save(equipo);
    }

    public void eliminarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new IncidenciaNotFoundException("Equipo no encontrado con id: " + id));
        equipo.setEstado(Estado.INACTIVO);
        equipoRepository.save(equipo);
    }

    public Optional<Equipo> obtenerEquipo(Long id) {
        return equipoRepository.findById(id);
    }

    public List<Equipo> listarEquipos() {
        return equipoRepository.findAll();
    }

    public List<Equipo> listarEquiposActivos() {
        return equipoRepository.findByEstado(Estado.ACTIVO);
    }

    public Page<Equipo> listarEquiposPaginado(Pageable pageable) {
        return equipoRepository.findAllByOrderByNombreAsc(pageable);
    }

    public Page<Equipo> buscarEquipos(String texto, Pageable pageable) {
        return equipoRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(
                texto, texto, pageable);
    }

    public Page<Equipo> buscarEquipos(String texto, Estado estado, Pageable pageable) {
        return equipoRepository.buscar(texto.trim(), estado, pageable);
    }

    public long contarPorTipo(TipoEquipo tipo) {
        return equipoRepository.countByTipo(tipo);
    }
}
