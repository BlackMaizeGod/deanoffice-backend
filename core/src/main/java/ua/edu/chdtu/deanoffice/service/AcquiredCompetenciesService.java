package ua.edu.chdtu.deanoffice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.AcquiredCompetencies;
import ua.edu.chdtu.deanoffice.repository.AcquiredCompetenciesRepository;

import java.util.List;

@Service
public class AcquiredCompetenciesService {
    private final AcquiredCompetenciesRepository acquiredCompetenciesRepository;

    @Autowired
    public AcquiredCompetenciesService(AcquiredCompetenciesRepository acquiredCompetenciesRepository) {
        this.acquiredCompetenciesRepository = acquiredCompetenciesRepository;
    }

    public AcquiredCompetencies getAcquiredCompetencies(int specializationId) {
        List<AcquiredCompetencies> acquiredCompetencies =
                acquiredCompetenciesRepository.findLastCompetenciesForSpecialization(specializationId);
        if (acquiredCompetencies.isEmpty()) {
            return new AcquiredCompetencies();
        }

        return acquiredCompetencies.get(0);
    }

    public AcquiredCompetencies findBySpecializationIdAndYear(Integer specializationId, Integer year) {
        return acquiredCompetenciesRepository.findBySpecializationIdAndYear(specializationId, year);
    }

    public void updateCompetenciesUkr(Integer acquiredCompetenciesId, String competencies) {
        AcquiredCompetencies acquiredCompetencies = this.getById(acquiredCompetenciesId);
        acquiredCompetencies.setCompetencies(competencies);
        this.acquiredCompetenciesRepository.save(acquiredCompetencies);
    }

    private AcquiredCompetencies getById(Integer acquiredCompetenciesId) {
        return this.acquiredCompetenciesRepository.findOne(acquiredCompetenciesId);
    }

    public void create(AcquiredCompetencies acquiredCompetencies) {
        this.acquiredCompetenciesRepository.save(acquiredCompetencies);
    }
}
