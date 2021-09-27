package bg.infosys.crc.dao.pub;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.entities.pub.Operator;
import bg.infosys.crc.entities.pub.ReportedBlocking;
import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.ndbo.TripleChart;

@Repository
public class ReportedBlockingDAO extends GenericDAOImpl<ReportedBlocking, Integer> {
	
	public long getReportedBlockingComparison(LocalDate from, LocalDate to, Integer countryId, Integer operatorId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<ReportedBlocking> root = cq.from(ReportedBlocking.class);

		cq.where(getReportedBlockingPredicates(from, to, countryId, operatorId, cb, root));
		cq.select(cb.countDistinct(root));
		
		try {
			return createQuery(cq).getSingleResult();
		} catch (NoResultException e) {
			return 0;
		}
		
	}
	
	public List<TripleChart> getReportedBlockingGrouped(LocalDate from, LocalDate to) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<TripleChart> query = cb.createQuery(TripleChart.class);
		Root<ReportedBlocking> root = query.from(ReportedBlocking.class);
		
		Path<Operator> operator = root.get(ReportedBlocking._operator);
		Path<Country> country = operator.get(Operator._country);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.between(root.get(ReportedRoaming._eventTs).as(LocalDate.class), from, to));
		query.where(predicates.toArray(new Predicate[predicates.size()]));
		
		query.multiselect(
			operator.get(_id),
			operator.get(Operator._name),
			operator.get(Operator._mnc),
			
			country.get(_id),
			country.get(Country._mcc),
			country.get(Country._nameBg),
			
			cb.count(root.get(ReportedRoaming._id)))
		.groupBy(
			operator.get(_id),
			operator.get(Operator._name),
			operator.get(Operator._mnc),
			
			country.get(_id),
			country.get(Country._mcc),
			country.get(Country._nameBg));
			
		return createQuery(query).getResultList();
	}
	
	public List<ReportedBlocking> findAllReportedBlockingsPaged(int firstResult, int pageSize, String sortBy, String sortDirection, LocalDate from, LocalDate to, Integer countryId, Integer operatorId, boolean getAll) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<ReportedBlocking> cq = cb.createQuery(ReportedBlocking.class);
		Root<ReportedBlocking> root	= cq.from(ReportedBlocking.class);
		
		root.fetch(ReportedBlocking._user);
		Fetch<ReportedBlocking, Operator> operatorFetch = root.fetch(ReportedBlocking._operator);
		operatorFetch.fetch(Operator._country);
		
		cq.where(getReportedBlockingPredicates(from, to, countryId, operatorId, cb, root));
		
		String[] parts = sortBy.split("\\.");
		if (parts.length == 1) {
			sortReportedBlockings(cq, cb, root.get(sortBy), sortDirection);
		} else {
			if(parts[0].equals("country")) {
				sortReportedBlockings(cq, cb, root.get(ReportedBlocking._operator).get(Operator._country).get(parts[parts.length-1]), sortDirection);
			} 
			if(parts[0].equals("operator")) {
				sortReportedBlockings(cq, cb, root.get(ReportedBlocking._operator).get(parts[parts.length-1]), sortDirection);
			} 
		}
		
		if (getAll) {
			return createQuery(cq).getResultList();
		} else {
			return createQuery(cq).setFirstResult(firstResult).setMaxResults(pageSize).getResultList();
		}
	}
	
	public long countReportedBlockings(LocalDate from, LocalDate to, Integer countryId, Integer operatorId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<ReportedBlocking> root = cq.from(ReportedBlocking.class);

		cq.where(getReportedBlockingPredicates(from, to, countryId, operatorId, cb, root));
		
		cq.select(cb.countDistinct(root));
		
		try {
			return createQuery(cq).getSingleResult();
		} catch (NoResultException e) {
			return 0;
		}
	}
	
	private Predicate[] getReportedBlockingPredicates(LocalDate from, LocalDate to, Integer countryId, Integer operatorId, CriteriaBuilder builder, Root<ReportedBlocking> root) {
		List<Predicate> where = new ArrayList<>();
		
		if (from != null && to != null) {
			where.add(builder.between(root.get(ReportedBlocking._eventTs).as(LocalDate.class), from, to));
		}
		
		if (countryId != null) {
			where.add(builder.equal(root.get(ReportedBlocking._operator).get(Operator._country).get(_id), countryId));
		}
		
		if (operatorId != null) {
			where.add(builder.equal(root.get(ReportedBlocking._operator).get(_id), operatorId));
		}
		
		return where.toArray(new Predicate[where.size()]);
	}
	
	private void sortReportedBlockings(CriteriaQuery<ReportedBlocking> cq, CriteriaBuilder cb, Path<Object> path, String sortDirection) {
		if (sortDirection.equalsIgnoreCase("asc")) {			
			cq.orderBy(cb.asc(path));
		} else {
			cq.orderBy(cb.desc(path));
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> countMonthlyGrouped(Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, Integer countryId, Integer operatorId) {
		StoredProcedureQuery function = getEntityManager().createStoredProcedureQuery("PUBLIC.COUNT_BLOCKINGS_MONTHLY_GR");

		function.registerStoredProcedureParameter("fromMonth", Integer.class, ParameterMode.IN);
		function.registerStoredProcedureParameter("fromYear", Integer.class, ParameterMode.IN);
		function.registerStoredProcedureParameter("toMonth", Integer.class, ParameterMode.IN);
		function.registerStoredProcedureParameter("toYear", Integer.class, ParameterMode.IN);

		function.registerStoredProcedureParameter("countryId", Integer.class, ParameterMode.IN);
		function.registerStoredProcedureParameter("operatorId", Integer.class, ParameterMode.IN);
		
		function.setParameter("fromMonth", fromMonth);
		function.setParameter("fromYear", fromYear);
		function.setParameter("toMonth", toMonth);
		function.setParameter("toYear", toYear);
		
		function.setParameter("countryId", countryId == null ? 0 : countryId);
		function.setParameter("operatorId", operatorId == null ? 0 : operatorId);
		
		function.execute();
		
		return function.getResultList();
	}
	
}
