package bg.infosys.crc.dao.mobile.security;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.mobile.security.ActiveUser;

@Repository
public class ActiveUserDAO extends GenericDAOImpl<ActiveUser, Integer> {

	public int delete(Integer userId, String key) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaDelete<ActiveUser> cd = cb.createCriteriaDelete(ActiveUser.class);
		Root<ActiveUser> root = cd.from(ActiveUser.class);
		
		cd.where(
			cb.equal(root.get(ActiveUser._userId), userId),
			cb.equal(root.get(ActiveUser._key), key));
		
		return createQuery(cd).executeUpdate();
	}
	
}
