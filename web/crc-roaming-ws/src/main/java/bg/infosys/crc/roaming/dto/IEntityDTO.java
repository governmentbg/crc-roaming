package bg.infosys.crc.roaming.dto;

public interface IEntityDTO<T> {
	T merge(T entity);
	T toEntity();
}
