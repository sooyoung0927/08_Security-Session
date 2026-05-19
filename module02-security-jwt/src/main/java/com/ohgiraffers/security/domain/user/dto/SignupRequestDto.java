package com.ohgiraffers.security.domain.user.dto;

import com.ohgiraffers.security.domain.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;


public class SignupRequestDto {

    @NotBlank(message = "사용자 아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 20, message = "사용자 아이디는 4자 이상 20자 이하로 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 6, max = 100, message = "비밀번호는 6자 이상 100자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Size(max = 50, message = "이메일은 50자 이하로 입력해주세요.")
    private String email;

    @NotEmpty(message = "역할은 최소 하나 이상 선택해야 합니다.") // List가 비어있지 않도록 검증
    private List<Role> roles;

    public SignupRequestDto() {
    }

    public SignupRequestDto(String username, String password, String email, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Role> getRoles() { // Getter 이름 변경
        return roles;
    }

    public void setRoles(List<Role> roles) { // Setter 이름 변경
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "SignupRequestDto{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", email='" + email + '\'' +
                ", roles=" + roles + // roles 리스트 직접 출력
                '}';
    }
}