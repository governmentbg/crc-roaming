package bg.infosys.crc.roaming.dto.web.journal;

import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import bg.infosys.crc.entities.web.JournalChange;
import bg.infosys.crc.entities.web.JournalObjectTypeEnum;
import bg.infosys.crc.entities.web.JournalOperationTypeEnum;
import bg.infosys.crc.roaming.dto.web.users.ListUserDTO;

public class ListJournalDTO {
	private Integer id;
	private JsonNode initialState;
	private JsonNode newState;
	private ListUserDTO editor;
	private String ts;
	private JournalObjectTypeEnum objType;
	private JournalOperationTypeEnum operationType;

	public ListJournalDTO(JournalChange jc, ObjectMapper mapper) {
		if (mapper != null) {
			try {
				this.initialState = mapper.readTree(jc.getInitialState());
				this.newState = mapper.readTree(jc.getNewState());
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}

		this.id		= jc.getId();
		this.editor = jc.getEditor() == null ? null : new ListUserDTO(jc.getEditor(), true);
		this.ts		= jc.getTs().format(DateTimeFormatter.ISO_DATE_TIME);
		this.objType	= jc.getObjType();
		this.operationType	= jc.getOperationType();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public JsonNode getInitialState() {
		return initialState;
	}

	public JsonNode getNewState() {
		return newState;
	}

	public ListUserDTO getEditor() {
		return editor;
	}

	public String getTs() {
		return ts;
	}

	public JournalObjectTypeEnum getObjType() {
		return objType;
	}
	
	public JournalOperationTypeEnum getOperationType() {
		return operationType;
	}

}
