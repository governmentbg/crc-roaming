package bg.infosys.crc.roaming.components.controllers.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.roaming.dto.web.operators.AddEditOperatorDTO;
import bg.infosys.crc.roaming.dto.web.operators.ListOperatorDTO;
import bg.infosys.crc.roaming.helpers.web.Permissions;
import bg.infosys.crc.roaming.services.web.OperatorService;

@RestController
@RequestMapping(value = "/api/web/operators")
public class OperatorController {
	@Autowired private OperatorService operatorService;

	@Secured(Permissions.VIEW_OPERATORS)
	@GetMapping(value = "/get/{id}")
	public List<ListOperatorDTO> getOperatorsByCountry(@PathVariable Integer id) {
		return operatorService.getOperatorsByCountry(id);
	}
	
	@Secured(Permissions.EDIT_OPERATORS)
	@PostMapping(value = "/add")
	public void addOperator(@RequestBody AddEditOperatorDTO operator) {
		operatorService.saveOperator(operator);
	}
	
	@Secured(Permissions.EDIT_OPERATORS)
	@PostMapping("/edit/{id}")
	public void updateOperator(@PathVariable Integer id, @RequestBody AddEditOperatorDTO operator) {
		operatorService.updateOperator(id, operator);
	}
	
	@Secured(Permissions.EDIT_OPERATORS)
	@DeleteMapping("/delete/{id}")
	public void deleteOperator(@PathVariable Integer id) {
		operatorService.deleteOperator(id);
	}
}
