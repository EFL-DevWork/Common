package com.thoughtworks.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.config.CryptoConfig;
import com.thoughtworks.StringEncryptor;
import com.thoughtworks.user.api.UserRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private String userId;


    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "dob")
    private String dob;

    @Convert(converter = StringEncryptor.class)
    @Column(name = "tax_id")
    private String taxId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password")
    private String password;

    public User(UserRequest userReq) {
        this.userId = userReq.userId;
        this.firstName = userReq.firstName;
        this.lastName = userReq.lastName;
        this.dob = userReq.dob;
        this.taxId = userReq.taxId;
        this.password = CryptoConfig.get().hash(userReq.password);
    }

//    public User() {
//    }
}
