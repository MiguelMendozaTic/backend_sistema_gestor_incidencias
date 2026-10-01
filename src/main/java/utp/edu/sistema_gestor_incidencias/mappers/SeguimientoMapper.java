package utp.edu.sistema_gestor_incidencias.mappers;

import org.springframework.stereotype.Component;

import utp.edu.sistema_gestor_incidencias.dto.seguimiento.SeguimientoDTO;
import utp.edu.sistema_gestor_incidencias.dto.seguimiento.SeguimientoResponseDto;
import utp.edu.sistema_gestor_incidencias.model.Incidencia;
import utp.edu.sistema_gestor_incidencias.model.Seguimiento;

@Component
public class SeguimientoMapper {
	public Seguimiento toEntity(SeguimientoDTO dto) {
		if(dto == null) return null;
		Seguimiento seguimiento = new Seguimiento();
		seguimiento.setComentario(dto.getComentario());
		seguimiento.setEstado(dto.getEstado());
		
		Incidencia incidencia = new Incidencia();
		incidencia.setId(dto.getIdIncidencia());
		seguimiento.setIncidencia(incidencia);
		
		return seguimiento;
	}
	
	public SeguimientoResponseDto toResponseDto(Seguimiento seguimiento) {
		if(seguimiento == null) return null;
		SeguimientoResponseDto dto = new SeguimientoResponseDto();
		dto.setId(seguimiento.getIdSeguimiento());
		dto.setComentario(seguimiento.getComentario());
		dto.setEstado(seguimiento.getEstado());
		dto.setIncidenciaId(seguimiento.getIncidencia().getId());
		dto.setNombreUsuario(seguimiento.getUsuario().getUsername());
		dto.setFecha(seguimiento.getFecha());
		return dto;
	}
}
