package ua.edu.chdtu.deanoffice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;
import ua.edu.chdtu.deanoffice.entity.Faculty;
import ua.edu.chdtu.deanoffice.entity.Role;
import ua.edu.chdtu.deanoffice.entity.UserRole;
import ua.edu.chdtu.deanoffice.repository.ApplicationUserRepository;
import ua.edu.chdtu.deanoffice.repository.UserRoleRepository;
import java.util.List;

@Service
public class ApplicationUserService {
    private ApplicationUserRepository applicationUserRepository;
    private FacultyService facultyService;
    private UserRoleRepository userRoleRepository;

    @Autowired
    public ApplicationUserService(ApplicationUserRepository applicationUserRepository, FacultyService facultyService, UserRoleRepository userRoleRepository) {
        this.applicationUserRepository = applicationUserRepository;
        this.facultyService = facultyService;
        this.userRoleRepository = userRoleRepository;
    }

    public List<ApplicationUser> getAll() {
        return applicationUserRepository.findAll();
    }

    public void create(String firstName, String lastName, String username, String password, int facultyId) {
        Faculty faculty = facultyService.findById(facultyId);
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setFirstName(firstName);
        applicationUser.setLastName(lastName);
        applicationUser.setUsername(username);
        applicationUser.setPassword(password);
        applicationUser.setFaculty(faculty);
        ApplicationUser savedUser = applicationUserRepository.saveAndFlush(applicationUser);
        createDefaultRole(savedUser);
    }

    public ApplicationUser getById(long id){
        return applicationUserRepository.findById(id);
    }

    private void createDefaultRole(ApplicationUser applicationUser) {
        UserRole defaultRole = new UserRole();
        defaultRole.setUserId(applicationUser.getId());
        defaultRole.setRole(Role.DEAN_OFFICER);
        userRoleRepository.saveAndFlush(defaultRole);
    }

    public void update(ApplicationUser applicationUser, int facutltyId){
        Faculty faculty = facultyService.findById(facutltyId);
        ApplicationUser oldUser = applicationUserRepository.findById(applicationUser.getId());
        oldUser.setFirstName(applicationUser.getFirstName());
        oldUser.setLastName(applicationUser.getLastName());
        oldUser.setUsername(applicationUser.getUsername());
        oldUser.setFaculty(faculty);
        applicationUserRepository.save(oldUser);
    }

    public void delete(ApplicationUser applicationUser) {
        applicationUserRepository.delete(applicationUser);
    }
}
