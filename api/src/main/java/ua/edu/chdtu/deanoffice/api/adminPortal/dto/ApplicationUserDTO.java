package ua.edu.chdtu.deanoffice.api.adminPortal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationUserDTO {
    private long id;
    private String firstName;
    private String lastName;
    private String username;
    private FacultyDTO faculty;
}
