package bg.infosys.crc.entities.web.security;

import javax.persistence.Entity;
import javax.persistence.Table;

import bg.infosys.common.ws.security.db.entities.AbstractPasswordResetRequest;

@Entity
@Table(name = "password_reset_requests", schema = "web_security")
public class PasswordResetRequest extends AbstractPasswordResetRequest {

}
