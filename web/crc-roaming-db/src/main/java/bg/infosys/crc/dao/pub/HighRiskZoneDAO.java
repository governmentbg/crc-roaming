package bg.infosys.crc.dao.pub;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.HighRiskZone;

@Repository
public class HighRiskZoneDAO extends GenericDAOImpl<HighRiskZone, Integer> {

	public Long countRoamingZones() {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<HighRiskZone> root = cq.from(HighRiskZone.class);

		cq.select(cb.countDistinct(root));
		
		return createQuery(cq).getSingleResult();
	}
	
}
