package bg.infosys.crc.roaming.helpers.web;

import java.util.List;

public class PagingResultDTO<T> {
	private List<T> content;
	private int pageNumber;
	private int pageSize;
	private int fromRow;
	private int toRow;

	public PagingResultDTO() {
	}

	public PagingResultDTO(List<T> content, int pageNumber, int pageSize) {
		this.content = content;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		if (content.size() > 0) {
			this.fromRow = (pageNumber - 1) * pageSize + 1;
			this.toRow = fromRow + content.size() - 1;
		}
	}

	public List<T> getContent() {
		return content;
	}
	
	public int getPageNumber() {
		return pageNumber;
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public int getFromRow() {
		return fromRow;
	}

	public int getToRow() {
		return toRow;
	}

}
