package com.wanted.securitysession.domain.auth.config;

import com.wanted.securitysession.domain.auth.handler.AuthFailHandler;
import com.wanted.securitysession.domain.auth.handler.AuthSuccessHandler;
import com.wanted.securitysession.domain.user.model.dao.LoginLogRepository;
import com.wanted.securitysession.domain.user.model.service.MemberService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
     * 비밀번호를 인코딩하기 위한 Bean
     * Bcrypt는 비밀번호 해싱에 가장 많이 사용되는 알고리즘 중 하나이다.
     *
     * 사용 이유
     * 1. 보안성 : 해시 함수에 무작위 솔트를 적용하여 생성한다.
     * 2. 비용 증가 : 매개변수에 값을 주면 암호 생성 시간을 조절할 수 있어 무차별 공격을 어렵게 한다.
     * 3. 호환성 : 높은 보안 수준 및 데이터베이스에 저장하기 쉬운 특징
     * 4. 알고리즘 신뢰성 : 보안에 대한 검증과 평가를 거친 알고리즘으로 문제없이 계속 사용 중
     * */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * - 세션이 생성될 때: HttpSessionEventPublisher가 SessionRegistry에 세션을 등록
     * - 세션이 소멸될 때: HttpSessionEventPublisher가 SessionRegistry에서 세션을 제거
     *
     * 이를 통해 SessionRegistry는 현재 활성화된 세션 목록을 정확하게 유지할 수 있다.
     * */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    // 정적 리소스에 대한 요청을 제외하겠다는 설정이다. (static 파일 하위)
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    /**
     * AuthFailHandler Bean 생성
     * SecurityConfig에서 직접 생성하여 순환 의존성 방지
     *
     * 로그인 실패 시
     * 1. 실패 횟수 증가
     * 2. 실패 로그 저장
     * */
    @Bean
    public AuthFailHandler authFailHandler(MemberService memberService, LoginLogRepository loginLogRepository) {
        return new AuthFailHandler(memberService, loginLogRepository);
    }

    /**
     * AuthSuccessHandler Bean 생성
     *
     * 로그인 성공 시
     * 1. 실패 횟수 초기화
     * 2. 성공 로그 저장
     * */
    @Bean
    public AuthSuccessHandler authSuccessHandler(MemberService memberService, LoginLogRepository loginLogRepository) {
        return new AuthSuccessHandler(memberService, loginLogRepository);
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http,
                                         AuthFailHandler authFailHandler,
                                         AuthSuccessHandler authSuccessHandler,
                                         SessionRegistry sessionRegistry) throws Exception {

        http.authorizeHttpRequests(auth -> { // 서버의 리소스에 접근 가능한 권한을 설정함
                    auth.requestMatchers("/auth/login", "/user/signup", "/auth/fail", "/").permitAll();
                    auth.requestMatchers("/admin/*").hasAnyAuthority("ADMIN");
                    auth.requestMatchers("/user/*").hasAnyAuthority("USER", "ADMIN");
                    auth.anyRequest().authenticated();
                }).formLogin(login -> {
                    login.loginPage("/auth/login"); // 로그인 페이지에 해당되는 서블릿이 존재해야 한다.
                    login.usernameParameter("user"); // 사용자 id 입력 필드 (input의 name과 일치)
                    login.passwordParameter("pass"); // 사용자 pass 입력 필드 (input의 name과 일치)
                    login.successHandler(authSuccessHandler); // 로그인 성공 시 실패 횟수 초기화 및 성공 로그 저장
                    login.failureHandler(authFailHandler);    // 로그인 실패 시 실패 횟수 증가 및 실패 로그 저장
                }).rememberMe(rememberMe -> { // Remember Me 기능 추가
                    rememberMe.rememberMeParameter("remember-me"); // 체크박스 name 속성과 일치
                    rememberMe.tokenValiditySeconds(86400); // 토큰 유효 기간: 24시간 (초 단위)
                    rememberMe.key("remember-me-secret-key"); // Remember Me 토큰 생성에 사용되는 Key
                }).logout(logout -> {
                    logout.logoutUrl("/auth/logout"); // 로그아웃 처리 URL 설정
                    logout.deleteCookies("JSESSIONID"); // 로그아웃 시 사용자의 JSESSIONID 삭제
                    logout.deleteCookies("remember-me"); // Remember Me 쿠키 삭제
                    logout.invalidateHttpSession(true); // 세션을 소멸하도록 허용하는 것
                    logout.logoutSuccessUrl("/"); // 로그아웃 시 이동할 페이지 설정
                }).sessionManagement(session -> {
                    session.maximumSessions(1).sessionRegistry(sessionRegistry); // session의 허용 개수를 제한
                    session.invalidSessionUrl("/"); // 세션 만료 시 이동할 페이지
                })
                /**
                 * CSRF(Cross-Site Request Forgery) 보호 설정
                 *
                 * CSRF란?
                 * - 공격자가 사용자의 권한을 이용하여 악의적인 요청을 전송하는 공격
                 * - 예: 사용자가 로그인한 상태에서 공격자의 악성 사이트 방문 시,
                 *   자동으로 은행 송금 요청이 실행되는 경우
                 *
                 * 현재 disable() 한 이유:
                 * 1. 강의/학습 목적: 코드의 복잡도를 줄이기 위해 임시 비활성화
                 * 2. 세션 기반 폼 제출: 일반적인 HTML 폼으로 POST 요청할 때마다
                 *    CSRF 토큰을 포함해야 하는데, 설정이 필요함
                 * 3. Postman/REST 클라이언트 테스트 용이: API 테스트 시 CSRF 토큰 처리 불필요
                 *
                 * 프로덕션 환경에서는:
                 * - disable() 제거 또는 .csrf(csrf -> csrf.disable()) 대신
                 * - Thymeleaf의 _csrf 토큰을 폼에 포함하거나
                 * - CSRF 토큰을 명시적으로 설정하여 보호해야 함
                 *
                 * 보안 권장사항:
                 * ✓ 세션 기반 인증 사용 시 -> CSRF 보호 필수
                 * ✓ JWT 토큰 기반 인증 시 -> CSRF 보호 불필요 (Stateless)
                 * */
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}