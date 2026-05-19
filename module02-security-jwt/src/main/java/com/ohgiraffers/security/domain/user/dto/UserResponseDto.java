package com.ohgiraffers.security.domain.user.dto;

import com.ohgiraffers.security.domain.user.entity.User;
import com.ohgiraffers.security.domain.user.model.Role;

import java.util.List;


public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private List<Role> role;

    // 기본 생성자
    public UserResponseDto() {
    }

    // User 엔티티를 DTO로 변환하는 생성자
    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRoles();
    }

    // 모든 필드를 받는 생성자 (필요시)
    public UserResponseDto(Long id, String username, String email, List<Role> role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }


    // Getter 메소드
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<Role> getRole() {
        return role;
    }

    // Setter 메소드 (필요시)
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(List<Role> role) {
        this.role = role;
    }


    @Override
    public String toString() {
        return "UserResponseDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + (role != null ? role.stream().toList() : "null") +
                '}';
    }
}