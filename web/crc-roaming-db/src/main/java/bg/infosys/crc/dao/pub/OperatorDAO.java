package bg.infosys.crc.dao.pub;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.entities.pub.Operator;
import bg.infosys.crc.entities.web.security.User;

@Repository
public class OperatorDAO extends GenericDAOImpl<Operator, Integer> {

	public List<Operator> findAll(String orderBy, Boolean asc) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Operator> cq = cb.createQuery(Operator.class);
		Root<Operator> root	= cq.from(Operator.class);
		
		root.fetch(Operator._country);
		root.fetch(Operator._createdBy, JoinType.LEFT);
		root.fetch(Operator._updatedBy, JoinType.LEFT);
		
		Path<?> orderPath = null;
		switch (orderBy) {
		case "id":
			orderPath = root.get(_id);
			break;
		case "opName":
			orderPath = root.get(Operator._name);
			break;
		case "cnName":
			orderPath = root.get(Operator._country).get(Country._nameBg);
			break;
		case "euMember":
			orderPath = root.get(Operator._country).get(Country._euMember);
			break;
		case "mnc":
			orderPath = root.get(Operator._mnc);
			break;
		case "mcc":
			orderPath = root.get(Operator._country).get(Country._mcc);
			break;
		case "createdBy":
			orderPath = root.get(Operator._createdBy).get(User._fullName);
			break;
		case "createdAt":
			orderPath = root.get(Operator._createdAt);
			break;
		case "updatedBy":
			orderPath = root.get(Operator._updatedBy).get(User._fullName);
			break;
		case "updatedAt":
			orderPath = root.get(Operator._updatedAt);
			break;
		}
		
		cq
			.select(root)
			.where(cb.isNull(root.get(Operator._deletedBy)))
			.orderBy(asc ? cb.asc(orderPath) : cb.desc(orderPath));
		
		return createQuery(cq).getResultList();
	}
	
	public List<Operator> findOperatorsByCountryOrdered(Integer countryId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Operator> cq = cb.createQuery(Operator.class);
		Root<Operator> root	= cq.from(Operator.class);
		
		root.fetch(Operator._country);
		root.fetch(Operator._createdBy, JoinType.LEFT);
		root.fetch(Operator._updatedBy, JoinType.LEFT);
		
		cq  
			.select(root)
			.where(
					cb.isNull(root.get(Operator._deletedBy)),
					cb.equal(root.get(Operator._country).get(_id), countryId))
			.orderBy(cb.desc(root.get(_id)));
		
		return createQuery(cq).getResultList();
	}
	
	public Operator findByMNCandMCC(String mnc, Short mcc) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Operator> crit = cb.createQuery(Operator.class);
		Root<Operator> root	= crit.from(Operator.class);
		
		Join<?, ?> country = root.join(Operator._country);
		
		crit
			.select(root)
		    .where(
		    	cb.equal(root.get(Operator._mnc), mnc),
		    	cb.equal(country.get(Country._mcc), mcc));
		
		try {
			return createQuery(crit).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public boolean nameExists(String name, Integer operatorId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> crit = cb.createQuery(Long.class);
		Root<Operator> root	= crit.from(Operator.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(Operator._name), name));
		predicates.add(cb.isNull(root.get(Operator._deletedBy)));
		if (operatorId != null) {
			predicates.add(cb.notEqual(root.get(_id), operatorId));
		}
		
		crit
			.select(cb.count(root))
			.where(predicates.toArray(new Predicate[predicates.size()]));
		
		return createQuery(crit).getSingleResult() > 0;
	}
	
	public boolean mncCountryComboExists(String mnc, Integer countryId, Integer operatorId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> crit = cb.createQuery(Long.class);
		Root<Operator> root	= crit.from(Operator.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(Operator._mnc), mnc));
		predicates.add(cb.equal(root.get(Operator._country).get(_id), countryId));
		predicates.add(cb.isNull(root.get(Operator._deletedBy)));
		if (operatorId != null) {
			predicates.add(cb.notEqual(root.get(_id), operatorId));
		}
		
		crit
			.select(cb.count(root))
			.where(predicates.toArray(new Predicate[predicates.size()]));
		
		return createQuery(crit).getSingleResult() > 0;
	}

}
