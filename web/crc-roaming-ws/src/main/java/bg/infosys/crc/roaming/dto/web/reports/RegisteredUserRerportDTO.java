package bg.infosys.crc.roaming.dto.web.reports;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegisteredUserRerportDTO {
	private Long numberOfRegisteredUsers;
	private String dateOfLastCreatedUser;
	
	public RegisteredUserRerportDTO(Long countOfUsers, LocalDateTime craetedAt) {
		this.numberOfRegisteredUsers = countOfUsers;
		this.dateOfLastCreatedUser = craetedAt == null ? null : craetedAt.format(DateTimeFormatter.ISO_DATE_TIME);
	}

	public Long getNumberOfRegisteredUsers() {
		return numberOfRegisteredUsers;
	}

	public void setNumberOfRegisteredUsers(Long numberOfRegisteredUsers) {
		this.numberOfRegisteredUsers = numberOfRegisteredUsers;
	}

	public String getDateOfLastCreatedUser() {
		return dateOfLastCreatedUser;
	}

	public void setDateOfLastCreatedUser(String dateOfLastCreatedUser) {
		this.dateOfLastCreatedUser = dateOfLastCreatedUser;
	}
}
