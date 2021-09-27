package bg.infosys.crc.dao.web.security;

import org.springframework.stereotype.Repository;

import bg.infosys.common.ws.security.db.dao.AbstractPasswordResetRequestDAO;
import bg.infosys.crc.entities.web.security.PasswordResetRequest;

@Repository
public class PasswordResetRequestDAO extends AbstractPasswordResetRequestDAO<PasswordResetRequest>{

}
