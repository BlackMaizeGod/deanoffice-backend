package ua.edu.chdtu.deanoffice.service;

import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.Faculty;
import ua.edu.chdtu.deanoffice.repository.FacultyRepository;
import java.util.List;

@Service
public class FacultyService {
    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public void checkStudentDegree(Integer studentDegreeId, Integer facultyId) throws Exception {
        Integer realFacultyId = facultyRepository.findIdByStudent(studentDegreeId);
        compareFacultyIds(facultyId, realFacultyId);
    }

    public void checkGroup(Integer studentGroupId, Integer facultyId) throws Exception {
        Integer realFacultyId = facultyRepository.findIdByGroup(studentGroupId);
        compareFacultyIds(facultyId, realFacultyId);
        return;
    }

    private void compareFacultyIds(Integer facultyId, Integer realFacultyId) throws Exception {
        if (realFacultyId == null) {
            throw new Exception("404");
        } else if (realFacultyId.equals(facultyId)) {
            return;
        } else {
            throw new Exception("403");
        }
    }

    public List<Faculty> getAll(){
        return facultyRepository.findAll();
    }

    public Faculty findById(int id){
        return facultyRepository.findById(id);
    }


    public Faculty getByName(String name) {
        return facultyRepository.findByName(name);
    }
}
