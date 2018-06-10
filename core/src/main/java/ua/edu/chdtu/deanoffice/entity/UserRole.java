package ua.edu.chdtu.deanoffice.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Table(name = "user_role")
@Getter
@Setter
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(name = "app_user_id")
    protected Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    protected Role role;
}