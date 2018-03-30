package ua.edu.chdtu.deanoffice.api.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.chdtu.deanoffice.entity.Degree;
import ua.edu.chdtu.deanoffice.service.DegreeService;

import java.util.List;

import static ua.edu.chdtu.deanoffice.api.general.Util.parseToNanedDTO;

@RestController
@RequestMapping("/degrees")
public class DegreeController {
    private final DegreeService degreeService;

    @Autowired
    public DegreeController(DegreeService degreeService) {
        this.degreeService = degreeService;
    }

    @GetMapping("")
    public ResponseEntity getDegrees() {
        List<Degree> degrees = degreeService.getDegrees();
        return ResponseEntity.ok(parseToNanedDTO(degrees));
    }
}
