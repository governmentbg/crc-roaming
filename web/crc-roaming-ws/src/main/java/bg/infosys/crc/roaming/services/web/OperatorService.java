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
import bg.infosys.crc.dao.pub.OperatorDAO;
import bg.infosys.crc.dao.pub.SettingDAO;
import bg.infosys.crc.dao.web.security.UserDAO;
import bg.infosys.crc.entities.pub.Operator;
import bg.infosys.crc.entities.pub.Setting;
import bg.infosys.crc.entities.pub.SettingEnum;
import bg.infosys.crc.roaming.dto.web.operators.AddEditOperatorDTO;
import bg.infosys.crc.roaming.dto.web.operators.ListOperatorDTO;

@Service
public class OperatorService {
	@Autowired private OperatorDAO	operatorDAO;
	@Autowired private UserDAO		userDAO;
	@Autowired private SettingDAO	settingDAO;

	public List<ListOperatorDTO> getOperators(String orderBy, Boolean asc) {
		return operatorDAO.findAll(orderBy, asc).stream()
			.map(o -> new ListOperatorDTO(o))
			.collect(Collectors.toList());
	}


	public List<ListOperatorDTO> getOperatorsByCountry(Integer countryId) {
		return operatorDAO.findOperatorsByCountryOrdered(countryId).stream()
				.map(o -> new ListOperatorDTO(o))
				.collect(Collectors.toList());
	}
	
	@Transactional
	public void saveOperator(AddEditOperatorDTO operator) {
		//checkIfNameExists(operator.getName(), null);
		checkIfMncCountryUnique(operator.getMnc(), operator.getCountryId(), null);
		
		Operator o = operator.toEntity();
		o.setCreatedBy(userDAO.findById(SecuritySession.getUserId()));
		o.setCreatedAt(LocalDateTime.now());

		updateTelcosVn();
		operatorDAO.save(o);
	}
	
	@Transactional
	public void updateOperator(Integer operatorId, AddEditOperatorDTO operator) {
		Operator o = getAndCheckIfOperatorExists(operatorId);
		//checkIfNameExists(operator.getName(), operatorId);
		checkIfMncCountryUnique(operator.getMnc(), operator.getCountryId(), operatorId);
		
		o.setUpdatedBy(userDAO.findById(SecuritySession.getUserId()));
		o.setUpdatedAt(LocalDateTime.now());
		
		updateTelcosVn();
		operatorDAO.update(operator.merge(o));
	}
	
	@Transactional
	public void deleteOperator(Integer operatorId) {
		Operator o = getAndCheckIfOperatorExists(operatorId);
		
		o.setDeletedBy(userDAO.findById(SecuritySession.getUserId()));
		o.setDeletedAt(LocalDateTime.now());
		
		updateTelcosVn();
		operatorDAO.update(o);
	}
	
	private Operator getAndCheckIfOperatorExists(Integer id) {
		Operator t = operatorDAO.findById(id);
		if (t == null) {
			throw new ResponseStatusException(HttpServletResponse.SC_GONE, "The operator does not exist", "operatorDoesNotExist");
		}
		
		return t;
	}
	
//	private void checkIfNameExists(String name, Integer operatorId) {
//		if (operatorDAO.nameExists(name, operatorId)) {
//			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT, "Operator name already exists", "operatorNameExists");
//		}
//	}
	
	private void checkIfMncCountryUnique(String mnc, Integer countryId, Integer operatorId) {
		if (operatorDAO.mncCountryComboExists(mnc, countryId, operatorId)) {
			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT, "MNC-Country combo already exist", "mncCountryComboExist");
		}
	}
	
	private void updateTelcosVn() {
		Setting telcosVn = settingDAO.findById(SettingEnum.TELCOS_VN);
		short vn = Short.parseShort(telcosVn.getValue());
		vn++;
		telcosVn.setValue(Short.toString(vn));
		settingDAO.update(telcosVn);
	}

}
