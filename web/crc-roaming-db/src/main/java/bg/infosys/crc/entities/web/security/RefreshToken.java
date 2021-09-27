package bg.infosys.crc.entities.web.security;

import javax.persistence.Entity;
import javax.persistence.Table;

import bg.infosys.common.ws.security.db.entities.AbstractRefreshToken;

@Entity
@Table(name = "refresh_tokens", schema = "web_security")
public class RefreshToken extends AbstractRefreshToken {

}
