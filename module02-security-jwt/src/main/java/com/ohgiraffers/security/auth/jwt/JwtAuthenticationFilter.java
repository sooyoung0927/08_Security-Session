package com.ohgiraffers.security.auth.jwt;

import com.ohgiraffers.security.auth.token.service.RefreshService;
import com.ohgiraffers.security.exception.ExpiredJwtCustomException;
import com.ohgiraffers.security.exception.InvalidJwtCustomException;
import com.ohgiraffers.security.exception.InvalidRefreshTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
 JwtAuthenticationFilter는 사용자의 요청 헤더에서 JWT를 추출하여,
 해당 토큰이 유효하다면 사용자 정보를 기반으로 Authentication 객체를 생성하고
 SecurityContext에 등록해 Spring Security 인증 체인에 통합하는 역할을 한다.

 ✅ 동작 시점
 - UsernamePasswordAuthenticationFilter 앞에 위치하여 인증 이전에 JWT 기반 인증을 먼저 시도

 ✅ 필터 처리 순서
 1. 요청의 Authorization 헤더에서 JWT 추출
 2. 토큰 유효성 검증 (JwtTokenProvider.validateToken)
 3. 사용자 ID 추출 및 인증 객체 생성 (JwtTokenProvider.getAuthentication)
 4. SecurityContextHolder에 인증 정보 저장

 ✅ 설계 포인트
 - 인증 예외가 발생하더라도 필터 체인을 중단하지 않고 다음 필터로 전달
 - 토큰이 없거나 잘못된 경우에도 명확한 인증 실패 처리를 위해 후속 핸들러 활용 필요
*/
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter를 상속 받아서
    // 하나의 요청에 딱 한 번만 자동으로 실행

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshService refreshService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RefreshService refreshService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshService = refreshService;
    }

    // 로그인 후 프론트는 우리가 준 토큰값을 가지고 Authorization: Bearer eyJhbGci... 헤더를 붙여서 요청을 보냄
    // 그럼 이 흐름이 실행 됨
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = jwtTokenProvider.resolveToken(request);
        // "Authorization: Bearer <accessToken>"에서 추출
        // resolveToken = 요청 헤더에서 JWT 토큰 추출하는 역할
        // -> 프론트 쪽에서 온 요청에서 토큰 값을 추출해와서 변수에 저장하는 코드

        try {
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // Access Token이 유효한 경우
                // getAuthentication = 토큰에서 사용자 정보 추출하는 역할
                // 토큰 안에 담긴 사용자 정보를 가져옴
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtCustomException expiredAccessTokenException) {
            // Access Token이 만료된 경우, 자동 재발급 시도
            try {
                String refreshTokenValue = jwtTokenProvider.resolveRefreshToken(request); // "X-Refresh-Token" 헤더에서 추출
                if (refreshTokenValue != null) {
                    // AuthService를 통해 토큰 재발급 시도 (이 메서드는 이미 로테이션을 처리함)
                    String newTokens = refreshService.refreshAccessToken(refreshTokenValue);

                    // 재발급 성공 시, SecurityContext 업데이트 및 새 토큰들을 클라이언트에 전달
                    Authentication newAuthentication = jwtTokenProvider.getAuthentication(newTokens);
                    SecurityContextHolder.getContext().setAuthentication(newAuthentication);

                    // 클라이언트에게 새 토큰들을 응답 헤더로 전달
                    response.setHeader("New-Access-Token", newTokens);
                } else {
                    // 자동 재발급 시도했으나, 요청에 Refresh Token이 없는 경우
                    SecurityContextHolder.clearContext(); // 인증 정보 없음으로 처리
                }
            } catch (InvalidRefreshTokenException invalidRefreshTokenEx) {
                SecurityContextHolder.clearContext(); // 인증 정보 없음으로 처리
            } catch (Exception e) {
                // 토큰 재발급 과정에서 예상치 못한 다른 오류 발생 시
                SecurityContextHolder.clearContext();
            }
        } catch (InvalidJwtCustomException invalidAccessTokenException) {
            // Access Token이 만료 외의 이유로 유효하지 않은 경우 (예: 서명 오류, 잘못된 형식)
            SecurityContextHolder.clearContext();
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
