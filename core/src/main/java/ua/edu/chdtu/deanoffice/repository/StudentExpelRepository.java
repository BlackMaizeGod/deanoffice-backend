package ua.edu.chdtu.deanoffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.chdtu.deanoffice.entity.StudentExpel;

import java.util.Date;
import java.util.List;

public interface StudentExpelRepository extends JpaRepository<StudentExpel, Integer> {
    @Query("select se from StudentExpel se " +
            "where se.orderReason.id not in :success_reason_ids and " +
            "se.studentDegree.specialization.faculty.id = :faculty_id " +
            "and se.expelDate > :limit_date " +
            "and se.studentDegree.active = false " +
            "order by se.studentDegree.student.surname, se.studentDegree.student.name, " +
            "se.studentDegree.student.patronimic, se.studentDegree.studentGroup.name")
    List<StudentExpel> findAllFired(
            @Param("success_reason_ids") Integer[] successReasonId,
            @Param("limit_date") Date limitDate,
            @Param("faculty_id") Integer facultyId
    );

    @Query("select se from StudentExpel se " +
            "where se.id = :id and se.studentDegree.active = false ")
    StudentExpel findInactiveById(@Param("id") Integer studentExpelId);

    @Query("select se from StudentExpel se " +
            "where se.studentDegree.id in :student_degree_ids " +
            "and se.studentDegree.active = false")
    List<StudentExpel> findAllActiveFired(@Param("student_degree_ids") Integer[] studentDegreeIds);
}
