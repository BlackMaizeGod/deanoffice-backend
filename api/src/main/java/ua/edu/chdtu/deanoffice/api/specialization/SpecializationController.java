package ua.edu.chdtu.deanoffice.api.specialization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.chdtu.deanoffice.api.general.ExceptionHandlerAdvice;
import ua.edu.chdtu.deanoffice.api.specialization.dto.SpecializationDTO;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;
import ua.edu.chdtu.deanoffice.entity.Degree;
import ua.edu.chdtu.deanoffice.entity.Department;
import ua.edu.chdtu.deanoffice.entity.Faculty;
import ua.edu.chdtu.deanoffice.entity.Speciality;
import ua.edu.chdtu.deanoffice.entity.Specialization;
import ua.edu.chdtu.deanoffice.service.DegreeService;
import ua.edu.chdtu.deanoffice.service.DepartmentService;
import ua.edu.chdtu.deanoffice.service.SpecialityService;
import ua.edu.chdtu.deanoffice.service.SpecializationService;
import ua.edu.chdtu.deanoffice.webstarter.security.CurrentUser;

import java.net.URI;
import java.util.List;


import static ua.edu.chdtu.deanoffice.api.general.Util.getNewResourceLocation;
import static ua.edu.chdtu.deanoffice.api.general.parser.Parser.parse;
import static ua.edu.chdtu.deanoffice.api.general.parser.Parser.strictParse;

@RestController
@RequestMapping("/specializations")
public class SpecializationController {
    private final SpecializationService specializationService;
    private final SpecialityService specialityService;
    private final DepartmentService departmentService;
    private final DegreeService degreeService;

    @Autowired
    public SpecializationController(
            SpecializationService specializationService,
            SpecialityService specialityService,
            DepartmentService departmentService,
            DegreeService degreeService
    ) {
        this.specializationService = specializationService;
        this.specialityService = specialityService;
        this.departmentService = departmentService;
        this.degreeService = degreeService;
    }

    @GetMapping
    public ResponseEntity getSpecializationByActive(
            @RequestParam(value = "active", required = false, defaultValue = "true") boolean active,
            @CurrentUser ApplicationUser user
            ) {
        List<Specialization> specializations = specializationService.getAllByActive(active, user.getFaculty().getId());
        return ResponseEntity.ok(parse(specializations, SpecializationDTO.class));
    }

    @PostMapping
    public ResponseEntity createSpecialization(
            @RequestBody SpecializationDTO specializationDTO,
            @CurrentUser ApplicationUser user
    ) {
        try {
            Specialization specialization = create(specializationDTO, user.getFaculty());
            specialization = specializationService.save(specialization);

            URI location = getNewResourceLocation(specialization.getId());
            return ResponseEntity.created(location).body(specialization);
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private Specialization create(SpecializationDTO specializationDTO, Faculty faculty) {
        Specialization specialization = (Specialization) strictParse(specializationDTO, Specialization.class);

        Speciality speciality = this.specialityService.getById(specializationDTO.getSpecialityId());
        specialization.setSpeciality(speciality);

        Department department = departmentService.getById(specializationDTO.getDepartmentId());
        specialization.setDepartment(department);

        Degree degree = degreeService.getById(specializationDTO.getDegreeId());
        specialization.setDegree(degree);

        specialization.setFaculty(faculty);

        return specialization;
    }

    private ResponseEntity handleException(Exception exception) {
        return ExceptionHandlerAdvice.handleException(exception, SpecializationController.class);
    }

    @PutMapping("{specialization_id}")
    public ResponseEntity updateSpecialization(
            @RequestBody SpecializationDTO specializationDTO,
            @PathVariable("specialization_id") Integer specializationId,
            @CurrentUser ApplicationUser user
    ) {
        try {
            Specialization specialization = create(specializationDTO, user.getFaculty());
            specialization.setId(specializationId);
            specializationService.save(specialization);
            return ResponseEntity.ok().build();
        } catch (Exception exception) {
            return handleException(exception);
        }
    }
}
