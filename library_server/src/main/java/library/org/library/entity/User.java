package org.library.entity;

import jakarta.persistence.*;
import lombok.*;
import org.library.entity.enums.Role;
import org.library.entity.enums.Sex;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User implements UserDetails {

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

    @Transient
    private String confirmPassword;

    // ✅ Implement UserDetails methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> "ROLE_" + role.name());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Can be customized later
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Can be customized later
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Can be customized later
    }

    @Override
    public boolean isEnabled() {
        return true; // Can be customized later
    }
}
