package org.library.security;

import lombok.Getter;
import org.library.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // You can expand this if your app has multiple roles or permissions
        return Collections.singleton(() -> "ROLE_" + user.getRole().name());
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // make sure it's hashed!
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // or add a flag in User entity if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // same as above
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // likewise
    }

    @Override
    public boolean isEnabled() {
        return true; // you can hook this into a User field like `isActive`
    }
}
