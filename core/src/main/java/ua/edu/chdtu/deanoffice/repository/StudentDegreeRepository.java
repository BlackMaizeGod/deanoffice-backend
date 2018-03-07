package ua.edu.chdtu.deanoffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.chdtu.deanoffice.entity.StudentDegree;

import java.util.List;

public interface StudentDegreeRepository extends JpaRepository<StudentDegree, Integer> {
//TODO cr: потрібно структурувати запити, писати кожну логічну частину з нового рядка; не бачу сенсу використовувати довгі аліаси
    @Query("SELECT studentDegree from StudentDegree as studentDegree where studentDegree.active = :active and studentDegree.studentGroup.specialization.faculty.id = :facultyId order by studentDegree.student.surname, studentDegree.student.name, studentDegree.student.patronimic, studentDegree.studentGroup.name")
    List<StudentDegree> findAllByActiveForFacultyId(
            @Param("active") boolean active,
            @Param("facultyId") Integer facultyId
    );

//TODO cr: цей метод точно потрібен? коли практично він може бути використаний?
    @Query("SELECT studentDegree from StudentDegree as studentDegree where studentDegree.id in :student_degree_ids")
    List<StudentDegree> getAllByStudentDegreeIds(@Param("student_degree_ids") Integer[] studentDegreeIds);

    StudentDegree getById(Integer id);
}
