package bg.infosys.crc.dao.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.ws.security.db.dao.AbstractAuthorityDAO;
import bg.infosys.crc.entities.web.security.Authority;

@Repository
public class AuthorityDAO extends AbstractAuthorityDAO<Authority> {

	public Authority findByName(String name, Integer id) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Authority> crit = cb.createQuery(Authority.class);
		Root<Authority> root = crit.from(Authority.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(Authority._name), name));
		predicates.add(cb.isNull(root.get(Authority._deletedBy)));
		if (id != null) {
			predicates.add(cb.notEqual(root.get(_id), id));
		}
		
		crit
			.select(root)
			.where(predicates.toArray(new Predicate[predicates.size()]));
		
		try {
			return createQuery(crit).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Collection<Authority> findAllOrdered(boolean onlyValid) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Authority> crit = cb.createQuery(Authority.class);
		Root<Authority> root = crit.from(Authority.class);
		
		root.fetch(Authority._permissions, JoinType.LEFT);
		root.fetch(Authority._createdBy, JoinType.LEFT);
		root.fetch(Authority._updatedBy, JoinType.LEFT);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.isNull(root.get(Authority._deletedBy)));
		if (onlyValid) {
			predicates.add(cb.equal(root.get(Authority._enabled), true));
		}
		
		crit.select(root)
			.where(predicates.toArray(new Predicate[predicates.size()]))
			.orderBy(cb.desc(root.get(_id)))
			.distinct(true);
		
		return createQuery(crit).getResultList();
	}

}
