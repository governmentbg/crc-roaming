package bg.infosys.crc.dao.pub;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.SourceOperator;

@Repository
public class SourceOperatorDAO extends GenericDAOImpl<SourceOperator, Integer> {

	public SourceOperator find(String name, Short mnc, Short mcc) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<SourceOperator> crit = cb.createQuery(SourceOperator.class);
		Root<SourceOperator> root = crit.from(SourceOperator.class);
		
		crit
			.select(root)
		    .where(
		    	cb.equal(root.get(SourceOperator._mnc), mnc),
		    	cb.equal(root.get(SourceOperator._mcc), mcc),
		    	cb.equal(cb.lower(root.get(SourceOperator._name)), name.toLowerCase()));
		
		try {
			return createQuery(crit).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
