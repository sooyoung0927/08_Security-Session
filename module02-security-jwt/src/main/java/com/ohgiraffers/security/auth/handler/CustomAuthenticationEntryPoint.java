/*******************************************
 🛠 실습: CustomAuthenticationEntryPoint, CustomAccessDeniedHandler 구현
 ********************************************/

package com.ohgiraffers.security.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
✅ AuthenticationEntryPoint
- 인증되지 않은 사용자가 보호된 리소스에 접근할 경우 호출됨
- ex) 토큰이 없거나 유효하지 않은 경우
- 응답 코드: 401 Unauthorized
* */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /*
     * 인증 실패 처리
     * ex) 유효하지 않은 토큰, 토큰 없음, 인증되지 않은 요청
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{ \"error\": \"인증에 실패하였습니다. 토큰이 유효하지 않거나 없습니다.\" }");
    }
}

