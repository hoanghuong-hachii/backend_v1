package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
@Table(name = "users")
public class UserInfo {
    @Id
    private String idUser;

    @Size(max = Constants.NAME_MAX_LENGTH, min = Constants.NAME_MIN_LENGTH)
    //@Pattern(regexp = Constants.PATTERN_USERNAME)
    @Column(name = "userName", unique = true, nullable = false, length = Constants.NAME_MAX_LENGTH, columnDefinition = "nvarchar(1000)")
    private String userName;

    @NotNull
    @Size(max = Constants.PASSWORD_MAX_LENGTH, min = Constants.PASSWORD_MIN_LENGTH)
    @JsonIgnore
    @Column(name = "password", nullable = false, length = Constants.PASSWORD_MAX_LENGTH)
    private String password;

    @NotNull
    @Column(name = "email", nullable = false)
    @Email
    private String email;

    @NotNull
    @Column(name = "phoneNumber", nullable = false)
    @Pattern(regexp = Constants.PATTERN_PHONENUMBER)
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "nvarchar(1000)")
    private String address;
    @NotNull
    @Column(name = "roles", nullable = false)
    private String roles;

    @NotNull
    @Column(name = "gender", nullable = false, columnDefinition = "nvarchar(1000)")
    private String gender;

    @Column(name = "avatar", columnDefinition = "varchar(1000)")
    private String avatar;
    @Column(name = "background", columnDefinition = "varchar(1000)")
    private String background;
    // Mối quan hệ một-nhiều với Bill
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Bill> bills;

    public UserInfo(IDGenerator idGenerator){
        this.idUser = idGenerator.generateNextID();
     //   this.roles = "ROLE_USER";
    }
}
