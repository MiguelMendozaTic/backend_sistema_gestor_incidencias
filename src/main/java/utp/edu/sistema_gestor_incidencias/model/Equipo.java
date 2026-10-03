package utp.edu.sistema_gestor_incidencias.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import utp.edu.sistema_gestor_incidencias.enums.Area;
import utp.edu.sistema_gestor_incidencias.enums.Estado;
import utp.edu.sistema_gestor_incidencias.enums.TipoEquipo;

@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    // Si llega vacío, EquipoService lo genera (LAP-001, PC-002...).
    private String codigo;

    private String descripcion;

    @NotNull(message = "El tipo es obligatorio")
    @Enumerated(EnumType.STRING)
    private TipoEquipo tipo;

    @NotNull(message = "El área es obligatoria")
    @Enumerated(EnumType.STRING)
    private Area area;

    // Lo asigna EquipoService: ACTIVO al crear, se conserva si no se envía al editar.
    @Enumerated(EnumType.STRING)
    private Estado estado;

    public Equipo() {}

    public Equipo(Long id, String nombre, String codigo, String descripcion,
                  TipoEquipo tipo, Area area, Estado estado) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.area = area;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoEquipo getTipo() { return tipo; }
    public void setTipo(TipoEquipo tipo) { this.tipo = tipo; }

    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
