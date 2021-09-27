package bg.infosys.crc.dao.web.security;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.web.security.Permission;

@Repository
public class PermissionDAO extends GenericDAOImpl<Permission, Integer> {

}
