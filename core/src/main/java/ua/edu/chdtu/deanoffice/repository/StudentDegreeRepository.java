package ua.edu.chdtu.deanoffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.chdtu.deanoffice.entity.Student;
import ua.edu.chdtu.deanoffice.entity.StudentDegree;

import java.util.List;

public interface StudentDegreeRepository extends JpaRepository<StudentDegree, Integer> {
    @Query("SELECT sd from StudentDegree sd " +
            "where sd.active = :active " +
            "and sd.studentGroup.specialization.faculty.id = :facultyId " +
            "order by sd.student.surname, sd.student.name, sd.student.patronimic, sd.studentGroup.name")
    List<StudentDegree> findAllByActive(
            @Param("active") boolean active,
            @Param("facultyId") Integer facultyId
    );

    @Query("SELECT sd from StudentDegree sd " +
            "where sd.id in :student_degree_ids")
    List<StudentDegree> getAllByIds(@Param("student_degree_ids") List<Integer> studentDegreeIds);

    StudentDegree getById(Integer id);

    @Query("SELECT sd FROM StudentDegree sd " +
            "where sd.student.id = :student_id")
    List<StudentDegree> findAllByStudentId(@Param("student_id") Integer studentId);

    @Query("SELECT sd FROM StudentDegree sd " +
            "where sd.student.id = :student_id and sd.active = true")
    List<StudentDegree> findAllActiveByStudentId(@Param("student_id") Integer studentId);

    @Query("select sd from StudentDegree sd " +
            "where sd.studentGroup.id = :groupId and sd.active = :active " +
            "order by sd.student.surname, sd.student.name, sd.student.patronimic")
    List<StudentDegree> findStudentDegreeByStudentGroupIdAndActive(
            @Param("groupId") Integer groupId,
            @Param("active") boolean active
    );

    @Query("select s from StudentDegree sd " +
            "join sd.student s " +
            "where s.name like %:name% " +
            "and s.surname like %:surname% " +
            "and s.patronimic like %:patronimic% " +
            "and sd.specialization.faculty.id = :faculty_id " +
            "group by s " +
            "order by s.name, s.surname, s.patronimic")
    List<Student> findAllByFullNameUkr(
            @Param("name") String name,
            @Param("surname") String surname,
            @Param("patronimic") String patronimic,
            @Param("faculty_id") int facultyId
    );

    @Query("select s from Student s " +
            "where s.name = ':name' " +
            "and s.surname = ':surname' " +
            "and s.patronimic = ':patronimic' " +
            "group by s " +
            "order by s.name, s.surname, s.patronimic")
    List<Student> findAllByFullNameUkr(
            @Param("name") String name,
            @Param("surname") String surname,
            @Param("patronimic") String patronimic
    );
}
