package bg.infosys.crc.entities.web;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import bg.infosys.crc.entities.web.security.User;

@Entity
@Table(schema = "web", name = "journal_changes")
public class JournalChange {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "ts")
	private LocalDateTime ts;
	public static final String _ts = "ts";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "editor_id")
	private User editor;
	public static final String _editor = "editor";

	@Column(name = "initial_state", columnDefinition = "jsonb")
	private String initialState;

	@Column(name = "new_state", columnDefinition = "jsonb")
	private String newState;

	@Column(name = "object_type")
	@Enumerated(EnumType.ORDINAL)
	private JournalObjectTypeEnum objType;
	public static final String _objType = "objType";
	
	@Column(name = "operation_type")
	@Enumerated(EnumType.ORDINAL)
	private JournalOperationTypeEnum operationType;
	public static final String _operationType = "operationType";

	@Column(name = "record_id")
	private Integer recordId;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDateTime getTs() {
		return ts;
	}

	public void setTs(LocalDateTime ts) {
		this.ts = ts;
	}

	public User getEditor() {
		return editor;
	}

	public void setEditor(User editor) {
		this.editor = editor;
	}

	public String getInitialState() {
		return initialState;
	}

	public void setInitialState(String initialState) {
		this.initialState = initialState;
	}

	public String getNewState() {
		return newState;
	}

	public void setNewState(String newState) {
		this.newState = newState;
	}

	public JournalObjectTypeEnum getObjType() {
		return objType;
	}
	
	public void setObjType(JournalObjectTypeEnum objType) {
		this.objType = objType;
	}

	public Integer getRecordId() {
		return recordId;
	}

	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}
	
	public JournalOperationTypeEnum getOperationType() {
		return operationType;
	}
	
	public void setOperationType(JournalOperationTypeEnum operationType) {
		this.operationType = operationType;
	}

}
