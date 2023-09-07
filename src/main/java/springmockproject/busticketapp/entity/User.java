package springmockproject.busticketapp.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;


@Data
@NoArgsConstructor
@Entity
@Table(name = "RegUser")
public class User {
    @Id
    @Column(name = "Id")
    private String id;


    @Column(name = "FirstName")
    private String firstName;


    @Column(name = "LastName")
    private String lastName;


    @Column(name = "Password")
    private String password;

    @Transient
    private String confirm;

    @Transient
    private String oldPassword;

    @Column(name = "Role")
    private int role = 0;




}
