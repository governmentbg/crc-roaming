package bg.infosys.crc.dao.pub;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.entities.web.security.User;

@Repository
public class CountryDAO extends GenericDAOImpl<Country, Integer> {
	
	public List<Country> findAllOrdered(String orderBy, Boolean asc) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Country> cq = cb.createQuery(Country.class);
		Root<Country> root	= cq.from(Country.class);
		
		root.fetch(Country._createdBy, JoinType.LEFT);
		root.fetch(Country._updatedBy, JoinType.LEFT);
		
		Path<?> orderPath = null;
		switch (orderBy) {
		case "id":
			orderPath = root.get(_id);
			break;
		case "bgName":
			orderPath = root.get(Country._nameBg);
			break;
		case "intName":
			orderPath = root.get(Country._nameInt);
			break;
		case "mcc":
			orderPath = root.get(Country._mcc);
			break;
		case "phoneCode":
			orderPath = root.get(Country._phoneCode);
			break;
		case "euMember":
			orderPath = root.get(Country._euMember);
			break;
		case "createdBy":
			orderPath = root.get(Country._createdBy).get(User._fullName);
			break;
		case "createdAt":
			orderPath = root.get(Country._createdAt);
			break;
		case "updatedBy":
			orderPath = root.get(Country._updatedBy).get(User._fullName);
			break;
		case "updatedAt":
			orderPath = root.get(Country._updatedAt);
			break;
		}
		
		cq
			.select(root)
			.where(cb.isNull(root.get(Country._deletedBy)))
			.orderBy(asc ? cb.asc(orderPath) : cb.desc(orderPath));
		
		return createQuery(cq).getResultList();
	}

	public boolean exists(String name, Integer countryId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> crit = cb.createQuery(Long.class);
		Root<Country> root	= crit.from(Country.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.or(
			cb.equal(root.get(Country._nameBg), name),
			cb.equal(root.get(Country._nameInt), name)));
		predicates.add(cb.isNull(root.get(Country._deletedBy)));
		if (countryId != null) {
			predicates.add(cb.notEqual(root.get(_id), countryId));
		}
		
		crit
			.select(cb.count(root))
			.where(predicates.toArray(new Predicate[predicates.size()]));
		
		return createQuery(crit).getSingleResult() > 0;
	}
	
	public Country findByMCC(Short mcc) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Country> cq = cb.createQuery(Country.class);
		Root<Country> root	= cq.from(Country.class);
		
		cq
			.select(root)
			.where(cb.equal(root.get(Country._mcc), mcc));
		
		try {
			return createQuery(cq).getSingleResult();			
		} catch (NoResultException e) {
			return null;
		}
	}

}
