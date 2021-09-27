package bg.infosys.crc.dao.pub;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.BgRegion;
import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.entities.pub.Operator;
import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.ndbo.TripleChart;

@Repository
public class ReportedRoamingDAO extends GenericDAOImpl<ReportedRoaming, Integer> {

	public List<ReportedRoaming> getReportedRoamingsByUserId(Integer userId) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<ReportedRoaming> cq = cb.createQuery(ReportedRoaming.class);
		Root<ReportedRoaming> root = cq.from(ReportedRoaming.class);
		
		root.fetch(ReportedRoaming._operator).fetch(Operator._country);

		cq
			.select(root)
			.where(cb.equal(root.get(ReportedRoaming._user).get(_id), userId))
			.orderBy(cb.desc(root.get(ReportedRoaming._eventTs)));

		return createQuery(cq).getResultList();
	}

	public List<ReportedRoaming> getPrev(Integer userId) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<ReportedRoaming> cq = cb.createQuery(ReportedRoaming.class);
		Root<ReportedRoaming> root = cq.from(ReportedRoaming.class);

		cq.select(root)
		  .where(cb.equal(root.get(ReportedRoaming._user).get(_id), userId))
		  .orderBy(cb.desc(root.get(_id)));

		return createQuery(cq).setMaxResults(10).getResultList();
	}
	
	public List<ReportedRoaming> findAll(LocalDateTime from, LocalDateTime to) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<ReportedRoaming> cq = cb.createQuery(ReportedRoaming.class);
		Root<ReportedRoaming> root	= cq.from(ReportedRoaming.class);
		
		root.fetch(ReportedRoaming._user);
		root.fetch(ReportedRoaming._bgRegion);
		root.fetch(ReportedRoaming._operator).fetch(Operator._country);
		
		cq
			.where(
				cb.between(root.get(ReportedRoaming._eventTs), from, to),
				cb.isFalse(root.get(ReportedRoaming._hidden)))
			.orderBy(cb.asc(root.get(_id)));
		
		return createQuery(cq).getResultList();
	}
	
	public List<ReportedRoaming> findAllReportedRoamingsPaged(int firstResult, int pageSize, String sortBy, String sortDirection, LocalDate from, LocalDate to, Integer countryId, Integer operatorId, Integer regionId, boolean getAll) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<ReportedRoaming> cq = cb.createQuery(ReportedRoaming.class);
		Root<ReportedRoaming> root	= cq.from(ReportedRoaming.class);
		
		root.fetch(ReportedRoaming._user);
		root.fetch(ReportedRoaming._bgRegion);
		Fetch<ReportedRoaming, Operator> operatorFetch = root.fetch(ReportedRoaming._operator);
		operatorFetch.fetch(Operator._country);
		operatorFetch.fetch(Operator._createdBy);
		
		cq.where(getReportedRoamingPredicates(from, to, countryId, operatorId, regionId, cb, root));
		
		String[] parts = sortBy.split("\\.");
		if (parts.length == 1) {
			sortReportedRoamings(cq, cb, root.get(sortBy), sortDirection);
		} else {
			if (parts[0].equals("country")) {
				sortReportedRoamings(cq, cb, root.get(ReportedRoaming._operator).get(Operator._country).get(parts[parts.length-1]), sortDirection);
			}
			
			if (parts[0].equals("operator")) {
				sortReportedRoamings(cq, cb, root.get(ReportedRoaming._operator).get(parts[parts.length-1]), sortDirection);
			} 
		}
		
		if (getAll) {
			return createQuery(cq).getResultList();
		} else {
			return createQuery(cq).setFirstResult(firstResult).setMaxResults(pageSize).getResultList();
		}
	}
	
	private void sortReportedRoamings(CriteriaQuery<ReportedRoaming> cq, CriteriaBuilder cb, Path<Object> path, String sortDirection) {
		if (sortDirection.equalsIgnoreCase("asc")) {			
			cq.orderBy(cb.asc(path));
		} else {
			cq.orderBy(cb.desc(path));
		}
	}
	
	public long countReportedRoamings(LocalDate from, LocalDate to, Integer countryId, Integer operatorId, Integer regionId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<ReportedRoaming> root = cq.from(ReportedRoaming.class);

		cq.where(getReportedRoamingPredicates(from, to, countryId, operatorId, regionId, cb, root));
		cq.select(cb.countDistinct(root));
		
		return createQuery(cq).getSingleResult();
	}

	public List<ReportedRoaming> findAllMissingProvince() {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<ReportedRoaming> cq = cb.createQuery(ReportedRoaming.class);
		Root<ReportedRoaming> root = cq.from(ReportedRoaming.class);
		
		cq.select(root)
		  .where(cb.isNull(root.get(ReportedRoaming._bgRegion).get(_id)));

		return createQuery(cq).getResultList();
	}
	
	public List<ReportedRoaming> findReportedRoamings(LocalDate from, LocalDate to, Integer countryId, Integer operatorId, Integer regionId) {
		CriteriaBuilder cb					= criteriaBuilder();
		CriteriaQuery<ReportedRoaming> cq	= cb.createQuery(ReportedRoaming.class);
		Root<ReportedRoaming> root			= cq.from(ReportedRoaming.class);
		
		Fetch<?, ?> region		= root.fetch(ReportedRoaming._bgRegion);
		Fetch<?, ?> operator	= root.fetch(ReportedRoaming._operator);
		Fetch<?, ?> country		= operator.fetch(Operator._country);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.between(root.get(ReportedRoaming._eventTs).as(LocalDate.class), from, to));
		predicates.add(cb.isFalse(root.get(ReportedRoaming._hidden)));
		
		if (countryId != null) {
			predicates.add(cb.equal(((Join<?, ?>) country).get(_id), countryId));
		}
		
		if (operatorId != null) {
			predicates.add(cb.equal(((Join<?, ?>) operator).get(_id), operatorId));
		}
		
		if (regionId != null) {
			predicates.add(cb.equal(((Join<?, ?>) region).get(_id), regionId));
		}
		
		cq.where(predicates.toArray(new Predicate[predicates.size()]));
		
		return createQuery(cq).getResultList();
	}

	public List<TripleChart> getReportedRoamingGrouped(LocalDate from, LocalDate to) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<TripleChart> query = cb.createQuery(TripleChart.class);
		Root<ReportedRoaming> root = query.from(ReportedRoaming.class);
		
		Path<Operator> operator = root.get(ReportedRoaming._operator);
		Path<Country> country = operator.get(Operator._country);
		Path<BgRegion> region = root.get(ReportedRoaming._bgRegion);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.between(root.get(ReportedRoaming._eventTs).as(LocalDate.class), from, to));
		predicates.add(cb.isFalse(root.get(ReportedRoaming._hidden)));
		query.where(predicates.toArray(new Predicate[predicates.size()]));
		
		query.multiselect(
			operator.get(_id),
			operator.get(Operator._name),
			operator.get(Operator._mnc),
			
			country.get(_id),
			country.get(Country._mcc),
			country.get(Country._nameBg),
			
			region.get(_id),
			region.get(BgRegion._name),
			
			cb.count(root.get(ReportedRoaming._id)))
		
		.groupBy(
			operator.get(_id),
			operator.get(Operator._name),
			operator.get(Operator._mnc),
			
			country.get(_id),
			country.get(Country._mcc),
			country.get(Country._nameBg),
			
			region.get(_id),
			region.get(BgRegion._name));
			
		return createQuery(query).getResultList();
	}

	public long getReportedRoamingComparison(LocalDate from, LocalDate to, Integer countryId, Integer operatorId, Integer regionId) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<ReportedRoaming> root = cq.from(ReportedRoaming.class);

		cq.where(getReportedRoamingPredicates(from, to, countryId, operatorId, regionId, cb, root));
		cq.select(cb.countDistinct(root));
		
		try {
			return createQuery(cq).getSingleResult();
		} catch (NoResultException e) {
			return 0;
		}
		
	}
	
	private Predicate[] getReportedRoamingPredicates(LocalDate from, LocalDate to, Integer countryId, Integer operatorId, Integer regionId, CriteriaBuilder builder, Root<ReportedRoaming> root) {
		List<Predicate> where = new ArrayList<>();
		
		where.add(builder.notEqual(root.get(ReportedRoaming._hidden), true));
		
		if(from != null && to != null) {
			where.add(builder.between(root.get(ReportedRoaming._eventTs).as(LocalDate.class), from, to));
		}
		
		if(countryId != null) {
			where.add(builder.equal(root.get(ReportedRoaming._operator).get(Operator._country).get(_id), countryId));
		}
		
		if(operatorId != null) {
			where.add(builder.equal(root.get(ReportedRoaming._operator).get(_id), operatorId));
		}
		
		if(regionId != null) {
			where.add(builder.equal(root.get(ReportedRoaming._bgRegion).get(_id), regionId));
		}
		
		return where.toArray(new Predicate[where.size()]);
	}

	public List<ReportedRoaming> findAllRealSince(LocalDate since) {
		CriteriaBuilder cb					= criteriaBuilder();
		CriteriaQuery<ReportedRoaming> cq	= cb.createQuery(ReportedRoaming.class);
		Root<ReportedRoaming> root			= cq.from(ReportedRoaming.class);
		
		root.fetch(ReportedRoaming._operator).fetch(Operator._country);
		root.fetch(ReportedRoaming._sourceOperator); // Will fetch roaming events with source operator only
		
		cq.where(
			cb.isTrue(root.get(ReportedRoaming._realDetection)),
			cb.greaterThan(root.get(ReportedRoaming._eventTs).as(LocalDate.class), since));
		
		return createQuery(cq).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> countMonthlyGrouped(Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, Integer countryId, Integer operatorId, Integer regionId) {
		StoredProcedureQuery function = getEntityManager().createStoredProcedureQuery("PUBLIC.COUNT_ROAMINGS_MONTHLY_GR");

		function.registerStoredProcedureParameter("fromMonth", Integer.class, ParameterMode.IN);
		function.registerStoredProcedureParameter("fromYear", Integer.class, ParameterMode.IN);
		function.registerStoredProcedureParameter("toMonth", Integer.class, ParameterMode.IN);
		function.registerStoredProcedureParameter("toYear", Integer.class, ParameterMode.IN);

		function.registerStoredProcedureParameter("countryId", Integer.class, ParameterMode.IN);
		function.registerStoredProcedureParameter("operatorId", Integer.class, ParameterMode.IN);
		function.registerStoredProcedureParameter("regionId", Integer.class, ParameterMode.IN);
		
		function.setParameter("fromMonth", fromMonth);
		function.setParameter("fromYear", fromYear);
		function.setParameter("toMonth", toMonth);
		function.setParameter("toYear", toYear);
		
		function.setParameter("countryId", countryId == null ? 0 : countryId);
		function.setParameter("operatorId", operatorId == null ? 0 : operatorId);
		function.setParameter("regionId", regionId == null ? 0 : regionId);
		
		function.execute();
		
		return function.getResultList();
	}

	public int setHidden(LocalDate date) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaUpdate<ReportedRoaming> update = cb.createCriteriaUpdate(ReportedRoaming.class);
		Root<ReportedRoaming> root = update.from(ReportedRoaming.class);
		
		update.set(ReportedRoaming._hidden, true);
		update.where(
			cb.isFalse(root.get(ReportedRoaming._hidden)),
			cb.equal(root.get(ReportedRoaming._eventTs).as(LocalDate.class), date));
		
		return createQuery(update).executeUpdate();
	}
	
	public List<ReportedRoaming> findHidden() {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<ReportedRoaming> cq = cb.createQuery(ReportedRoaming.class);
		Root<ReportedRoaming> root	= cq.from(ReportedRoaming.class);
		
		root.fetch(ReportedRoaming._bgRegion);
		root.fetch(ReportedRoaming._operator).fetch(Operator._country);
		root.fetch(ReportedRoaming._sourceOperator);
		
		cq
			.where(cb.isTrue(root.get(ReportedRoaming._hidden)))
			.orderBy(cb.asc(root.get(_id)));
		
		return createQuery(cq).getResultList();
	}
	
}
