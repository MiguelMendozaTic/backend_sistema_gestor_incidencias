package utp.edu.sistema_gestor_incidencias.mappers;

import org.springframework.stereotype.Component;

import utp.edu.sistema_gestor_incidencias.dto.incidencia.IncidenciaResponseDTO;
import utp.edu.sistema_gestor_incidencias.model.Incidencia;
@Component
public class IncidenciaMapper {
	
	public IncidenciaResponseDTO toResponseDto(Incidencia incidencia) {
		if(incidencia == null) return null;
		
		IncidenciaResponseDTO dto = new IncidenciaResponseDTO();
		dto.setId(incidencia.getId());
		dto.setTitulo(incidencia.getTitulo());
		dto.setDescripcion(incidencia.getDescripcion());
		dto.setEstado(incidencia.getEstado());
		dto.setUsuarioId(incidencia.getUsuario().getId());
		
		if(incidencia.getTecnico()== null) {
			dto.setTecnicoId(null);
		}else {
			dto.setTecnicoId(incidencia.getTecnico().getId());
		}
		
		return dto;
	}
}
