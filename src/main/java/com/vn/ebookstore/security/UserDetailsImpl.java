package com.vn.ebookstore.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.vn.ebookstore.model.User;

public class UserDetailsImpl implements UserDetails {
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String avatar;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(User user, Collection<? extends GrantedAuthority> authorities) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.avatar = user.getAvatar();
        this.phoneNumber = user.getPhoneNumber();
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}