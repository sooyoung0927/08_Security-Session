package com.ohgiraffers.security.domain.user.entity;

import com.ohgiraffers.security.domain.user.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "jwt_login")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Convert(converter = RoleListConverter.class)
    @Column(name = "roles", nullable = false)
    private List<Role> roles = new ArrayList<>();

    protected User() {
    }

    private User(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.email = builder.email;
        this.roles = builder.roles != null ? new ArrayList<>(builder.roles) : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String rolesString = roles != null
                ? roles.stream().map(Role::name).collect(Collectors.joining(", "))
                : "";

        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", email='" + email + '\'' +
                ", roles=[" + rolesString + "]" +
                '}';
    }

    public static class Builder {
        private String username;
        private String password;
        private String email;
        private List<Role> roles = new ArrayList<>();

        private Builder() {
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder roles(List<Role> roles) {
            this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
            return this;
        }

        public Builder role(List<Role> roles) {
            if (roles != null && !roles.isEmpty()) {
                this.roles.addAll(roles);
            }
            return this;
        }

        public User build() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalStateException("사용자 이름은 필수입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalStateException("비밀번호는 필수입니다.");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalStateException("이메일은 필수입니다.");
            }
            if (roles == null || roles.isEmpty()) {
                throw new IllegalStateException("역할은 최소 하나 이상 필수입니다.");
            }
            return new User(this);
        }
    }
}
