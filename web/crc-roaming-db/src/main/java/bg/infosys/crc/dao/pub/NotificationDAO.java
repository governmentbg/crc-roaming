package bg.infosys.crc.dao.pub;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.Notification;

@Repository
public class NotificationDAO extends GenericDAOImpl<Notification, Integer> {

	public List<Notification> findAllPaged(int firstResult, int pageSize) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Notification> cq = cb.createQuery(Notification.class);
		Root<Notification> root = cq.from(Notification.class);
		
		root.fetch(Notification._createdBy);

		cq.select(root).orderBy(cb.desc(root.get(_id)));

		return createQuery(cq).setFirstResult(firstResult).setMaxResults(pageSize).getResultList();
	}
	
}
