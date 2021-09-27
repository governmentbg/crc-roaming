package bg.infosys.crc.dao.pub;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.Translation;
import bg.infosys.crc.entities.pub.Translation.TranslationId;

@Repository
public class TranslationDAO extends GenericDAOImpl<Translation, TranslationId> {
	
	public List<Translation> getAll() {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Translation> crit = cb.createQuery(Translation.class);
		Root<Translation> root	= crit.from(Translation.class);
		
		crit
			.select(root)
//			.where(cb.equal(root.get(Translation._language), language))
			.orderBy(cb.asc(root.get(Translation._id)));
	
		return createQuery(crit).getResultList();
	}

}