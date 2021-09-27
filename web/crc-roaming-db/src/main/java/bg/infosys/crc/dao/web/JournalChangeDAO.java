package bg.infosys.crc.dao.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.web.JournalChange;
import bg.infosys.crc.entities.web.JournalObjectTypeEnum;

@Repository
public class JournalChangeDAO extends GenericDAOImpl<JournalChange, Integer> {

	public List<JournalChange> find(int firstResult, Integer pageSize, LocalDate from, LocalDate to, Integer userId, JournalObjectTypeEnum objType) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<JournalChange> crit = cb.createQuery(JournalChange.class);
		Root<JournalChange> root = crit.from(JournalChange.class);
		
		List<Predicate> predicates = preparePredicates(cb, root, from, to, userId, objType);
		
		root.fetch(JournalChange._editor);
		
		crit
			.select(root)
			.where(predicates.toArray(new Predicate[predicates.size()]))
			.orderBy(cb.desc(root.get(_id)));
		
		return createQuery(crit).setFirstResult(firstResult).setMaxResults(pageSize).getResultList();
	}
	
	public long count(LocalDate from, LocalDate to, Integer userId, JournalObjectTypeEnum objType) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<JournalChange> root = cq.from(JournalChange.class);
		
		List<Predicate> predicates = preparePredicates(cb, root, from, to, userId, objType);
		cq.where(predicates.toArray(new Predicate[predicates.size()]));
		cq.select(cb.countDistinct(root));
		
		return createQuery(cq).getSingleResult();
	}
	
	private List<Predicate> preparePredicates(CriteriaBuilder cb, Root<JournalChange> root, LocalDate from, LocalDate to, Integer userId, JournalObjectTypeEnum objType) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.between(root.get(JournalChange._ts).as(LocalDate.class), from, to));
		if (userId != null) {
			predicates.add(cb.equal(root.get(JournalChange._editor), userId));
		}
		
		if (objType != null) {
			predicates.add(cb.equal(root.get(JournalChange._objType), objType));
		}
		
		return predicates;
	}
	
	public JournalChange getById(Integer id) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<JournalChange> crit = cb.createQuery(JournalChange.class);
		Root<JournalChange> root = crit.from(JournalChange.class);
		
		root.fetch(JournalChange._editor);
		crit
			.select(root)
			.where(cb.equal(root.get(_id), id));
		
		return createQuery(crit).getSingleResult();
	}

}
