package ua.edu.chdtu.deanoffice.entity;

import lombok.Getter;
import lombok.Setter;
import ua.edu.chdtu.deanoffice.entity.superclasses.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Getter
@Setter
@Entity
public class RenewedAcademicVacationStudent extends BaseEntity {
    @ManyToOne
    private StudentAcademicVacation studentAcademicVacation;
    private int studyYear;
    @Enumerated(value = EnumType.STRING)
    private Payment payment = Payment.BUDGET;
    @ManyToOne
    private StudentGroup studentGroup;
    @Temporal(TemporalType.DATE)
    private Date renewDate;
    @Temporal(TemporalType.DATE)
    private Date applicationDate;
}
