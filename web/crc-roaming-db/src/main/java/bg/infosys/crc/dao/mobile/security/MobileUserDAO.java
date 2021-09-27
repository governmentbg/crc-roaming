package bg.infosys.crc.dao.mobile.security;

import java.time.LocalDate;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.mobile.security.MobileUser;

@Repository
public class MobileUserDAO extends GenericDAOImpl<MobileUser, Integer> {
	
	public MobileUser findByEmail(String email) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<MobileUser> cq = cb.createQuery(MobileUser.class);
		Root<MobileUser> root = cq.from(MobileUser.class);
		
		cq
			.select(root)
			.where(cb.equal(root.get(MobileUser._email), email));
	
		try {
			return createQuery(cq).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
//	public MobileUser findByEmailAndLoginFrom(String email, Boolean passLogin) {
//		CriteriaBuilder cb = criteriaBuilder();
//		CriteriaQuery<MobileUser> cq = cb.createQuery(MobileUser.class);
//		Root<MobileUser> root	= cq.from(MobileUser.class);
//		
//		List<Predicate> where = new ArrayList<>(2);
//		where.add(cb.equal(root.get(MobileUser._email), email));
//		if (passLogin != null) {
//			where.add(cb.equal(root.get(MobileUser._passLogin), passLogin));
//		}
//		
//		cq
//			.select(root)
//			.where(where.toArray(new Predicate[where.size()]));
//	
//		try {
//			return createQuery(cq).getSingleResult();
//		} catch (NoResultException e) {
//			return null;
//		}
//	}

	public MobileUser getLastCreatedUserByCreatedAt(LocalDate from, LocalDate to) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<MobileUser> cq = cb.createQuery(MobileUser.class);
		Root<MobileUser> root	= cq.from(MobileUser.class);
		
		cq.select(root)
		  .where(cb.between(root.get(MobileUser._createdAt).as(LocalDate.class), from, to))
		  .orderBy(cb.desc(root.get(MobileUser._createdAt)));
	
		try {
			return createQuery(cq).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public long countRegisteredUsers(LocalDate from, LocalDate to) {
		CriteriaBuilder cb = criteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<MobileUser> root = cq.from(MobileUser.class);
		
		cq
			.select(cb.countDistinct(root))
			.where(cb.between(root.get(MobileUser._createdAt).as(LocalDate.class), from, to));
		
		return createQuery(cq).getSingleResult();
	}
}
