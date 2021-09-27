package bg.infosys.crc.roaming.helpers.web;

import java.util.List;

@Deprecated
public class PagingResultMatDTO<T> {
	private List<T> content;
	private long totalElements;
	private int pageSize;
	private int pageNumber;

	public PagingResultMatDTO() {
	}

	public PagingResultMatDTO(List<T> content, long totalElements, int pageNumber, int pageSize) {
		this.content = content;
		this.totalElements = totalElements;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
	}

	public List<T> getContent() {
		return content;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPageNumber() {
		return pageNumber;
	}
	
}
