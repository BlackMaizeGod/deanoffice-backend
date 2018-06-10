package ua.edu.chdtu.deanoffice.api.adminPortal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.chdtu.deanoffice.api.adminPortal.dto.ApplicationUserDTO;
import ua.edu.chdtu.deanoffice.api.adminPortal.dto.ApplicationUserRequestDTO;
import ua.edu.chdtu.deanoffice.api.general.ExceptionHandlerAdvice;
import ua.edu.chdtu.deanoffice.api.general.mapper.Mapper;
import ua.edu.chdtu.deanoffice.api.specialization.SpecializationController;
import ua.edu.chdtu.deanoffice.api.specialization.dto.SpecializationDTO;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;
import ua.edu.chdtu.deanoffice.entity.Specialization;
import ua.edu.chdtu.deanoffice.service.ApplicationUserService;
import ua.edu.chdtu.deanoffice.webstarter.security.CurrentUser;

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

    @PostMapping
    public ResponseEntity createApplicationUser(@RequestBody ApplicationUserRequestDTO applicationUserRequestDTO) {
        applicationUserService.create(applicationUserRequestDTO.getFirstName(),applicationUserRequestDTO.getLastName(), applicationUserRequestDTO.getUsername(), applicationUserRequestDTO.getPassword(), applicationUserRequestDTO.getFacultyId() );
        return ResponseEntity.ok().build();
    }

    @GetMapping("{applicationUserId}")
    public ResponseEntity getApplicationUserById(@PathVariable("applicationUserId") long applicationUserId) {
        return ResponseEntity.ok(Mapper.map(applicationUserService.getById(applicationUserId), ApplicationUserDTO.class));
    }

    @PutMapping
    public ResponseEntity updateSpecialization(@RequestBody ApplicationUserRequestDTO applicationUserRequestDTO) {
        ApplicationUser applicationUser = (ApplicationUser) Mapper.strictMap(applicationUserRequestDTO, ApplicationUser.class);
        applicationUserService.update(applicationUser, applicationUserRequestDTO.getFacultyId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{applicationUserId}")
    public ResponseEntity deleteApplicationUser(@PathVariable("applicationUserId") int applicationUserId) {
        ApplicationUser applicationUser = applicationUserService.getById(applicationUserId);
        if (applicationUser == null) {
            return ExceptionHandlerAdvice.handleException("User not found", ApplicationUserController.class, HttpStatus.NOT_FOUND);
        }
        try {
            applicationUserService.delete(applicationUser);
            return ResponseEntity.noContent().build();
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private ResponseEntity handleException(Exception exception) {
        return ExceptionHandlerAdvice.handleException(exception, ApplicationUserController.class);
    }
}
