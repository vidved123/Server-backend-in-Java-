package org.library.entity;

import jakarta.persistence.*;
import lombok.*;
import org.library.entity.enums.Role;
import org.library.entity.enums.Sex;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String username;
    private String password;
    private String email;
    private String mobileNumber;
    private String countryCode;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    // ✅ Add this field for password confirmation in forms
    @Transient
    private String confirmPassword;
}
