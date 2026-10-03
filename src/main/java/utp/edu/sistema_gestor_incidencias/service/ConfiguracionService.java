package utp.edu.sistema_gestor_incidencias.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.sistema_gestor_incidencias.dto.configuracion.ConfiguracionDTO;
import utp.edu.sistema_gestor_incidencias.model.ConfiguracionSistema;
import utp.edu.sistema_gestor_incidencias.repository.ConfiguracionRepository;

@Service
public class ConfiguracionService {

	private final ConfiguracionRepository configuracionRepository;

	public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
		this.configuracionRepository = configuracionRepository;
	}

	/** Funciona aunque la fila aún no exista: devuelve los valores por defecto. */
	public ConfiguracionDTO obtener() {
		ConfiguracionSistema c = configuracionRepository.findById(ConfiguracionSistema.ID)
				.orElseGet(ConfiguracionSistema::new);
		return new ConfiguracionDTO(c.getIntervaloRefresco());
	}

	@Transactional
	public ConfiguracionDTO actualizar(ConfiguracionDTO dto) {
		ConfiguracionSistema c = configuracionRepository.findById(ConfiguracionSistema.ID)
				.orElseGet(ConfiguracionSistema::new);
		c.setIntervaloRefresco(dto.getIntervaloRefresco());
		return new ConfiguracionDTO(configuracionRepository.save(c).getIntervaloRefresco());
	}
}
