package ua.edu.chdtu.deanoffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.chdtu.deanoffice.entity.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty, Integer> {
    @Query(value = "select f.id from student_degree sd " +
            "inner join student_group sg on sg.id = sd.student_group_id " +
            "inner join specialization s ON s.id = sg.specialization_id " +
            "inner join faculty f ON f.id = s.faculty_id " +
            "WHERE sd.id = :studentDegreeId", nativeQuery = true)
    Integer findIdByStudent(@Param("studentDegreeId") Integer studentDegreeId);
}
