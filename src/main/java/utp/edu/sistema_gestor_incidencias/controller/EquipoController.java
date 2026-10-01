package utp.edu.sistema_gestor_incidencias.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import utp.edu.sistema_gestor_incidencias.dto.ApiResponse;
import utp.edu.sistema_gestor_incidencias.enums.TipoEquipo;
import utp.edu.sistema_gestor_incidencias.model.Equipo;
import utp.edu.sistema_gestor_incidencias.service.EquipoService;

@RestController
@RequestMapping("/api/equipo")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Equipo>> crearEquipo(@Valid @RequestBody Equipo equipo) {
        Equipo nuevo = equipoService.crearEquipo(equipo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Equipo registrado con éxito", 201, nuevo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Equipo>> modificarEquipo(
            @PathVariable Long id, @Valid @RequestBody Equipo equipo) {
        Equipo modificado = equipoService.modificarEquipo(id, equipo);
        return ResponseEntity.ok(new ApiResponse<>(true, "Equipo actualizado", 200, modificado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarEquipo(@PathVariable Long id) {
        equipoService.eliminarEquipo(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Equipo desactivado", 200, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerEquipo(@PathVariable Long id) {
        var equipo = equipoService.obtenerEquipo(id);
        if (equipo.isPresent())
            return ResponseEntity.ok(equipo.get());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Equipo no encontrado con id: " + id);
    }

    @GetMapping
    public ResponseEntity<List<Equipo>> listarEquipos() {
        return ResponseEntity.ok(equipoService.listarEquipos());
    }

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<Equipo>>> listarEquiposActivos() {
        List<Equipo> equipos = equipoService.listarEquiposActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Equipos activos", 200, equipos));
    }

    @GetMapping("/paginado")
    public PagedModel<Equipo> listarEquiposPaginado(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "texto", defaultValue = "") String texto) {
        Pageable pageable = PageRequest.of(page, size);
        if (texto.trim().length() >= 2) {
            Page<Equipo> equipos = equipoService.buscarEquipos(texto, pageable);
            return new PagedModel<>(equipos);
        }
        Page<Equipo> equipos = equipoService.listarEquiposPaginado(pageable);
        return new PagedModel<>(equipos);
    }

    @GetMapping("/tipos")
    public ResponseEntity<TipoEquipo[]> listarTipos() {
        return ResponseEntity.ok(TipoEquipo.values());
    }

    @GetMapping("/preview-codigo")
    public ResponseEntity<String> previewCodigo(@RequestParam TipoEquipo tipo) {
        String codigo = equipoService.generarCodigo(tipo);
        return ResponseEntity.ok(codigo);
    }
}
