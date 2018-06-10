package ua.edu.chdtu.deanoffice.api.adminPortal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationUserRequestDTO {
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private int facultyId;
}
