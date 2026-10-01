package utp.edu.sistema_gestor_incidencias.enums;

public enum TipoEquipo {
    LAPTOP("LAP"),
    PC("PC"),
    IMPRESORA("IMP"),
    MONITOR("MON"),
    TECLADO("TEC"),
    MOUSE("MOU"),
    SCANNER("SCA"),
    PROYECTOR("PRO"),
    SERVIDOR("SRV"),
    OTRO("OTR");

    private final String prefijo;

    TipoEquipo(String prefijo) {
        this.prefijo = prefijo;
    }

    public String getPrefijo() {
        return prefijo;
    }
}
