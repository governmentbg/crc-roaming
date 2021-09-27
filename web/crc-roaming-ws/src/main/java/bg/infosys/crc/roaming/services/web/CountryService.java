package bg.infosys.crc.roaming.services.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bg.infosys.common.exceptions.ResponseStatusException;
import bg.infosys.common.ws.security.SecuritySession;
import bg.infosys.crc.dao.pub.CountryDAO;
import bg.infosys.crc.dao.web.security.UserDAO;
import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.roaming.dto.web.countries.AddEditCountryDTO;
import bg.infosys.crc.roaming.dto.web.countries.ListCountryDTO;

@Service
public class CountryService {
	@Autowired private CountryDAO	countryDAO;
	@Autowired private UserDAO		userDAO;

	public List<ListCountryDTO> getCountries(String orderBy, Boolean asc) {
		return countryDAO.findAllOrdered(orderBy, asc).stream()
			.map(c -> new ListCountryDTO(c, false))
			.collect(Collectors.toList());
	}
	
	public ListCountryDTO getCountry(Integer countryId) {
		return new ListCountryDTO(countryDAO.findById(countryId), false);
	}
	
	@Transactional
	public void saveCountry(AddEditCountryDTO country) {
		checkIfCountryExists(country.getNameBg(), null);
		checkIfCountryExists(country.getNameInt(), null);
		
		Country c = country.toEntity();
		c.setCreatedBy(userDAO.findById(SecuritySession.getUserId()));
		c.setCreatedAt(LocalDateTime.now());
		
		countryDAO.save(c);
	}
	
	@Transactional
	public void updateCountry(Integer countryId, AddEditCountryDTO country) {
		Country c = getAndCheckIfCountryExists(countryId);
		checkIfCountryExists(country.getNameBg(), countryId);
		checkIfCountryExists(country.getNameInt(), countryId);
		
		c.setUpdatedBy(userDAO.findById(SecuritySession.getUserId()));
		c.setUpdatedAt(LocalDateTime.now());
		
		countryDAO.update(country.merge(c));
	}
	
	@Transactional
	public void deleteCountry(Integer countryId) {
		Country c = getAndCheckIfCountryExists(countryId);
		c.setDeletedBy(userDAO.findById(SecuritySession.getUserId()));
		c.setDeletedAt(LocalDateTime.now());
		
		countryDAO.update(c);
	}
	
	private Country getAndCheckIfCountryExists(Integer id) {
		Country c = countryDAO.findById(id);
		if (c == null) {
			throw new ResponseStatusException(HttpServletResponse.SC_GONE, "The country does not exist", "countryDoesNotExist");
		}
		
		return c;
	}
	
	private void checkIfCountryExists(String country, Integer countryId) {
		if (countryDAO.exists(country, countryId)) {
			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT, "Country already exists", "countryNameExists");
		}
	}
	
}
