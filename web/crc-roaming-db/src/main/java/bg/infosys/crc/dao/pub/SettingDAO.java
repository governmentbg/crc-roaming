package bg.infosys.crc.dao.pub;

import org.springframework.stereotype.Repository;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.Setting;
import bg.infosys.crc.entities.pub.SettingEnum;

@Repository
public class SettingDAO extends GenericDAOImpl<Setting, SettingEnum> {
	
}