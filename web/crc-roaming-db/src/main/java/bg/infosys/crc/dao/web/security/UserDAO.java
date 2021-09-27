package bg.infosys.crc.dao.web.security;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.ws.security.db.dao.AbstractUserDAO;
import bg.infosys.crc.entities.web.security.Authority;
import bg.infosys.crc.entities.web.security.User;

@Repository
public class UserDAO extends AbstractUserDAO<User, Authority> {
	
	public User findUserById(Integer userId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<User> crit = cb.createQuery(User.class);
		Root<User> root	= crit.from(User.class);

		root.fetch(User._grantedAuthorities);
		
		crit.select(root);
		crit.where(cb.equal(root.get(_id), userId));
		
		try {
			return createQuery(crit).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public List<User> findAllPaged(int firstResult, int pageSize) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<User> crit = cb.createQuery(User.class);
		Root<User> root	= crit.from(User.class);
		
		root.fetch(User._grantedAuthorities);
		crit.orderBy(cb.desc(root.get(_id)));
		
		return createQuery(crit).setFirstResult(firstResult).setMaxResults(pageSize).getResultList();
	}
	
	@Deprecated
	public List<User> findAllPaged(int firstResult, int pageSize, String sortBy, String sortDirection) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<User> crit = cb.createQuery(User.class);
		Root<User> root	= crit.from(User.class);
		
		root.fetch(User._grantedAuthorities, JoinType.LEFT);

		if(sortDirection.equalsIgnoreCase("DESC")) {			
			crit.orderBy(cb.desc(root.get(sortBy)));
		} else {
			crit.orderBy(cb.asc(root.get(sortBy)));
		}
		
		return createQuery(crit).setFirstResult(firstResult).setMaxResults(pageSize).getResultList();
	}

	public User findByUsername(String username, Integer userId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<User> crit = cb.createQuery(User.class);
		Root<User> root	= crit.from(User.class);
		
		crit.select(root);
		if (userId != null) {
			crit.where(
				cb.equal(root.get(User._username), username),
				cb.notEqual(root.get(_id), userId));
		} else {
			crit.where(cb.equal(root.get(User._username), username));
		}
		
		try {
			return createQuery(crit).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public long countUsingAuhtority(Integer authorityId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> crit = cb.createQuery(Long.class);
		Root<User> root = crit.from(User.class);
		
		crit
			.select(cb.count(root))
			.where(cb.equal(root.join(User._grantedAuthorities).get(_id), authorityId));
		
		return createQuery(crit).getSingleResult();
	}

}