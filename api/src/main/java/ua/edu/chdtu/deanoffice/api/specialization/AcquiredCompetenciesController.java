package ua.edu.chdtu.deanoffice.api.specialization;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.chdtu.deanoffice.api.general.ExceptionHandlerAdvice;
import ua.edu.chdtu.deanoffice.api.specialization.dto.AcquiredCompetenciesDTO;
import ua.edu.chdtu.deanoffice.api.specialization.dto.SpecializationView;
import ua.edu.chdtu.deanoffice.entity.AcquiredCompetencies;
import ua.edu.chdtu.deanoffice.service.AcquiredCompetenciesService;

import static ua.edu.chdtu.deanoffice.api.general.mapper.Mapper.map;

@RestController
public class AcquiredCompetenciesController {
    private final AcquiredCompetenciesService acquiredCompetenciesService;

    @Autowired
    public AcquiredCompetenciesController(AcquiredCompetenciesService acquiredCompetenciesService) {
        this.acquiredCompetenciesService = acquiredCompetenciesService;
    }

    @GetMapping("/specializations/{specialization_id}/competencies/ukr")
    @JsonView(SpecializationView.AcquiredCompetenciesUkr.class)
    public ResponseEntity getCompetenciesForSpecialization(@PathVariable("specialization_id") int specializationId) {
        AcquiredCompetencies acquiredCompetencies = acquiredCompetenciesService.getAcquiredCompetencies(specializationId);
        return ResponseEntity.ok(map(acquiredCompetencies, AcquiredCompetenciesDTO.class));
    }

    @PutMapping("/acquired-competencies/{acquired-competencies-id}/ukr")
    public ResponseEntity updateAcquiredCompetenciesUkr(
            @PathVariable("acquired-competencies-id") Integer acquiredCompetenciesId,
            @RequestBody String competencies
    ) {
        try {
            acquiredCompetenciesService.updateCompetenciesUkr(acquiredCompetenciesId, competencies);
            return ResponseEntity.ok().build();
        } catch (Exception exception) {
            return ExceptionHandlerAdvice.handleException(exception, AcquiredCompetenciesController.class);
        }
    }
}