package ua.edu.chdtu.deanoffice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;
import ua.edu.chdtu.deanoffice.repository.ApplicationUserRepository;
import java.util.List;

@Service
public class ApplicationUserService {
    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    public List<ApplicationUser> getAll() {
        return applicationUserRepository.findAll();
    }
}
