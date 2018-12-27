package ua.edu.chdtu.deanoffice.service.stipend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.repository.StudentDegreeRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class StipendService {
    private final StudentDegreeRepository studentDegreeRepository;

    @Autowired
    public StipendService(StudentDegreeRepository studentDegreeRepository) {
        this.studentDegreeRepository = studentDegreeRepository;
    }

    public void getDebtorStudents() {

    }

    public List<DebtorStudentDegreesBean> getDebtorStudentDegrees(int facultyId, int degreeId, boolean active) {
        List<Object[]> rawData = studentDegreeRepository.findDebtorStudentDegreesRaw(degreeId, facultyId, active, "FULL_TIME", "BUDGET");
        List<DebtorStudentDegreesBean> debtorStudentDegreesBeans = new ArrayList<>(rawData.size());
        rawData.forEach(item -> debtorStudentDegreesBeans.add(new DebtorStudentDegreesBean(
                (Integer)item[0]/*degreeId*/,
                (String)item[1]/*surname*/,
                (String)item[2]/*name*/,
                (String)item[3]/*patronimic*/,
                (String)item[4]/*degreeName*/,
                (String)item[5]/*groupName*/,
                (Integer)item[6]/*year*/,
                (String)item[7]/*tuitionTerm*/,
                (String)item[8]/*specialityCode*/,
                (String)item[9]/*specialityName*/,
                (String)item[10]/*specializationName*/,
                (String)item[11]/*departmentAbbreviation*/,
                /*(Double)item[12]*/0/*averageCode*/,
                (String)item[13]/*courseName*/,
                (String)item[14]/*knowledgeControlName*/,
                (Integer)item[15]/*semester*/
        )));
        return debtorStudentDegreesBeans;
    }
}
