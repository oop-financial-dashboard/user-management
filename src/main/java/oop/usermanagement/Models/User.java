package oop.usermanagement.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotBlank
    private String email;
    @NotBlank
    private String password;

    private ERole role;

    public User (String email, String password) {
        this.email = email;
        this.password = password;

    }

    public long getId() {
        return id;
    };
}
