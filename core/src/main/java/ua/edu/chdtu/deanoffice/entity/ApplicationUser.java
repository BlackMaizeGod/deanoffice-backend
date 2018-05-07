package ua.edu.chdtu.deanoffice.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String password;
    private String firstName;
    private String lastName;
    private String username;

    @ManyToOne
    private Faculty faculty;

    @OneToMany(fetch=FetchType.EAGER)
    @JoinColumn(name="app_user_id", referencedColumnName="id")
    private List<UserRole> roles;
}
