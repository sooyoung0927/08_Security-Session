package com.ohgiraffers.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 로그인 요청 시 사용될 DTO 입니다.
 */
public class LoginRequestDto {

    @NotBlank(message = "사용자 아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 20, message = "사용자 아이디는 4자 이상 20자 이하로 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 6, message = "비밀번호는 6자 이상으로 입력해주세요.")
    private String password;

    // 기본 생성자 (JSON 바인딩을 위해 Jackson 등이 필요로 할 수 있음)
    public LoginRequestDto() {
    }

    // 모든 필드를 포함하는 생성자
    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter 메소드
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Setter 메소드
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequestDto{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" + // 보안을 위해 비밀번호는 로그에 남기지 않음
                '}';
    }
}