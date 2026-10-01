package utp.edu.sistema_gestor_incidencias.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utp.edu.sistema_gestor_incidencias.model.Role;
import utp.edu.sistema_gestor_incidencias.service.RoleService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/role")
public class RoleController {
  private final RoleService roleService;

	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}

  @GetMapping("")
  public List<Role> listaRoles() {
      return roleService.listarRoles();
  }
  

}
