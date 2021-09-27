package bg.infosys.crc.dao.pub;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.BgRegion;

@Repository
public class BgRegionDAO extends GenericDAOImpl<BgRegion, Integer> {
	
	public List<BgRegion> findAllOrdered() {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<BgRegion> cq = cb.createQuery(BgRegion.class);
		Root<BgRegion> root = cq.from(BgRegion.class);
		
		cq
			.select(root)
			.orderBy(cb.asc(root.get(BgRegion._name)));
		
		return createQuery(cq).getResultList();
	}

	public BgRegion findByNominatimName(String name) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<BgRegion> cq = cb.createQuery(BgRegion.class);
		Root<BgRegion> root = cq.from(BgRegion.class);
		
		cq
			.select(root)
			.where(cb.equal(cb.lower(root.get(BgRegion._nominatimName)), name.trim().toLowerCase()));
		
		try {
			return createQuery(cq).getSingleResult();			
		} catch (NoResultException e) {
			return null;
		}
	}

}
