package bg.infosys.crc.dao.web.security;

import org.springframework.stereotype.Repository;

import bg.infosys.common.ws.security.db.dao.AbstractRefreshTokenDAO;
import bg.infosys.crc.entities.web.security.RefreshToken;

@Repository
public class RefreshTokenDAO extends AbstractRefreshTokenDAO<RefreshToken> {

}
