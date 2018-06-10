package ua.edu.chdtu.deanoffice.api.faculty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.chdtu.deanoffice.api.faculty.dto.FacultyDTO;
import ua.edu.chdtu.deanoffice.api.general.mapper.Mapper;
import ua.edu.chdtu.deanoffice.service.FacultyService;

@RestController
@RequestMapping("/faculties")
public class FacultyController {
    @Autowired
    FacultyService facultyService;

    @GetMapping
    public ResponseEntity getAllSpecialities() {
        return ResponseEntity.ok(Mapper.map(facultyService.getAll(), FacultyDTO.class));
    }
}
