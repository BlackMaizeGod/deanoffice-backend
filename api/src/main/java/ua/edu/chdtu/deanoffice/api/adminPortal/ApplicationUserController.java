package ua.edu.chdtu.deanoffice.api.adminPortal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.chdtu.deanoffice.api.adminPortal.dto.ApplicationUserDTO;
import ua.edu.chdtu.deanoffice.api.general.mapper.Mapper;
import ua.edu.chdtu.deanoffice.service.ApplicationUserService;

@RestController
@RequestMapping("/applicationUser")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ApplicationUserController {
    @Autowired
    ApplicationUserService applicationUserService;

    @GetMapping
    public ResponseEntity getApplicationUsers() {
        return ResponseEntity.ok(Mapper.map(applicationUserService.getAll(), ApplicationUserDTO.class));
    }
}
