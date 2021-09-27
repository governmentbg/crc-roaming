package bg.infosys.crc.roaming.components.controllers.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.roaming.dto.web.countries.AddEditCountryDTO;
import bg.infosys.crc.roaming.dto.web.countries.ListCountryDTO;
import bg.infosys.crc.roaming.helpers.web.Permissions;
import bg.infosys.crc.roaming.services.web.CountryService;

@RestController
@RequestMapping(value = "/api/web/countries")
public class CountryController {
	@Autowired private CountryService countryService;

	@Secured(Permissions.VIEW_COUNTRIES)
	@GetMapping(value = "/get/{id}")
	public ListCountryDTO getCountry(@PathVariable Integer id) {
		return countryService.getCountry(id);
	}

	@Secured(Permissions.EDIT_COUNTRIES)
	@PostMapping(value = "/add")
	public void addCountry(@RequestBody AddEditCountryDTO country) {
		countryService.saveCountry(country);
	}
	
	@Secured(Permissions.EDIT_COUNTRIES)
	@PostMapping("/edit/{id}")
	public void updateCountry(@PathVariable Integer id, @RequestBody AddEditCountryDTO country) {
		countryService.updateCountry(id, country);
	}
	
	@Secured(Permissions.EDIT_COUNTRIES)
	@DeleteMapping("/delete/{id}")
	public void deleteCountry(@PathVariable Integer id) {
		countryService.deleteCountry(id);
	}
}
