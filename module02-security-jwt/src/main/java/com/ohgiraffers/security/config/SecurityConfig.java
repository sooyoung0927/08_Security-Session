package com.ohgiraffers.security.config;

import com.ohgiraffers.security.auth.handler.CustomAccessDeniedHandler;
import com.ohgiraffers.security.auth.handler.CustomAuthenticationEntryPoint;
import com.ohgiraffers.security.auth.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/*******************************************
 📖 개념: SecurityFilterChain 설정의 목적과 구성
 ********************************************/

/*
 Spring Security 기본 설정은 formLogin + 세션 인증 방식이다.
 JWT 기반 인증을 사용하기 위해 다음을 비활성화 및 커스터마이징해야 한다:

 - formLogin, logout, sessionManagement, csrf 등을 disable
 - Stateless 정책 적용
 - JwtAuthenticationFilter 등록
 - 인증 실패/권한 거부 핸들러 등록

 ✅ 핵심 개념 요약:
 - SecurityFilterChain: 요청 → 필터 → 인증/인가 → 컨트롤러 로직 처리
 - Stateless 환경에선 세션이 없기 때문에 토큰 기반 인증 필터가 필수
 - 인증 예외 응답 커스터마이징 필요
*/
/*******************************************
 🛠 실습: SecurityConfig.java 설정
 ********************************************/

@Configuration
@EnableWebSecurity                     // URL 경로 기반 필터 보안
// 모든 요청에 Security 설정 적용을 활성화
@EnableMethodSecurity(                 // 메서드 보안 활성화
        prePostEnabled = true          // @PreAuthorize / @PostAuthorize SpEL을 사용하면 역할(Role) 기반 검사뿐만 아니라, 현재 인증된 사용자 정보까지 정의 가능
        // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')") , @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        // 등을 활용하여 Actor 의 권한 별 접근 여부를 설정할 수 있다.
)
public class SecurityConfig {

    /*comment
    *  JWT 관련 커스텀 필터, 인증/인가 실패시 예외처리 클래스 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter; //입구에 서서 JWT를 검사하는 경비원 "이 종이 진짜야? 유통기한 지났어?" 확인
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }


    /*comment
     *  8080 백에드 서버와 5173 프론트 서버와 연관관련 설정
     *  cors -> 서로 다른 origin 연결 */
    /**
     * CORS 설정을 위한 {@link CorsConfigurationSource} 빈을 정의
     * 애플리케이션의 모든 경로("/**")에 대해 CORS 규칙을 적용.
     * @return {@link CorsConfigurationSource} 객체
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ 허용할 출처(Origin) 패턴 설정
        // 예: 프론트엔드 개발 서버(localhost:5173), 실제 배포된 프론트엔드 도메인
        // "*" 대신 구체적인 도메인이나 패턴을 사용하는 것이 보안상 좋다.
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:5173", // React, Vue 등의 개발서버
                "http://localhost:8081", // 다른 로컬 개발 환경
                "https://your-production-frontend.com" // 실제 서비스 프론트엔드 도메인
                // "*" // 모든 출처 허용 (개발 초기에는 편리하나, 프로덕션에서는 특정 출처만 허용 권장)
        ));

        // ✅ 허용할 HTTP 메소드 설정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // ✅ 요청에서 허용할 HTTP 헤더 설정
        // "Authorization" (JWT 토큰 전송), "Content-Type" 등 필요한 헤더를 명시
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Refresh-Token" // 리프레시 토큰을 위한 커스텀 헤더
                /*
                *  X-Refresh-Token이 여기 없으면,
                * 프론트가 이 헤더를 보내려 해도 브라우저가 차단합니다.
                * 이 헤더는 JwtAuthenticationFilter에서 request.getHeader("X-Refresh-Token")으로 꺼내 씁니다.
                * 즉 필터에서 꺼내 쓰려면 CORS에서 먼저 허용해줘야 합니다.
                * */
        ));

        // ✅ 클라이언트(브라우저)에게 노출할 수 있는 응답 헤더 설정
        // JWT 토큰을 응답 헤더로 전달하는 경우(예: 토큰 재발급 시) 해당 헤더를 명시해야
        // 클라이언트 JavaScript에서 접근 가능
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "New-Access-Token" // 새 액세스 토큰 전달용 커스텀 헤더
                /*
                서버가 응답할 때 보내는 헤더 중 프론트 JavaScript가 읽을 수 있는 헤더 목록입니다.
                브라우저는 기본적으로 커스텀 응답 헤더를 JS에서 읽지 못하게 막습니다.
                New-Access-Token이 여기 없으면, 서버가 재발급한 토큰을 응답 헤더에 담아 보내도
                프론트 JS가 response.headers.get('New-Access-Token')으로 읽을 수가 없습니다.
                JwtAuthenticationFilter에서 response.setHeader("New-Access-Token", newTokens)로 보내는 바로 그 값입니다
                */
        ));

        // ✅ 자격 증명(쿠키, Authorization 헤더 등)을 허용할지 여부 설정
        // true로 설정해야 쿠키를 사용한 인증이나 Authorization 헤더를 통한 토큰 인증이 가능.
        configuration.setAllowCredentials(true);
        //Authorization 헤더(JWT 토큰)를 요청에 포함해도 된다는 허가입니다.
        // 이게 false면 브라우저가 인증 관련 헤더를 아예 안 보냅니다. JWT 인증이 통째로 안 됩니다.


        // ✅ OPTIONS 사전 요청(Preflight Request)의 결과를 캐시할 시간(초 단위) 설정
        configuration.setMaxAge(3600L); // 1시간

        // UrlBasedCorsConfigurationSource 객체를 생성하고,
        // 모든 경로("/**")에 대해 위에서 정의한 CORS 설정을 등록합니다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
        // 이 빈은 아래 securityFilterChain()에서 http.cors(...)에 의해 자동으로 참조됩니다.
    }


    /*
     * AuthenticationManager 설정! 🔐
     * Spring Security에서 AuthenticationManager는 인증(로그인) 과정을 총괄하는 중요한 클래스이다.
     * 사용자가 보낸 아이디와 비밀번호 같은 인증 정보를 받아서,
     * 진짜 사용자인지 아닌지 확인하는 복잡한 과정을 이 처리하게 된다.
     *
     * AuthenticationConfiguration 객체를 받아서 getAuthenticationManager() 메서드로
     * AuthenticationManager를 가져오는 방식다.
     * 스프링 부트가 Security 설정을 자동으로 해줄 때 사용하는 설정 정보라고 보면 된다.
     *
     * 요렇게 설정해두면 Spring Security 필터들이 인증이 필요할 때 이 친구를 찾아와서
     * "이 사용자가 맞는지 확인 좀 해주세요!" 하고 부탁하게 된다.
-     *
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
        // AuthenticationManager는 "아이디/비밀번호 맞는지 확인하는 심사관"입니다.
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // "비밀번호 암호화 기계 만들기"
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /*
        * 서드를 연결해서 경비 규칙을 만들고,
        * 마지막에 .build()를 호출하면 완성된 경비 시스템(SecurityFilterChain)이 만들어집니다.
        * */

        /*
        * csrf (Cross-Site Request Forgery)
        *  인증된 사용자(로그인된 사용자)의 권한을 도용하여
        *  사용자가 의도하지 않은 요청을 웹 서버에 보내도록 만드는 공격
        * > 사용자의 쿠키 값을 이용하여 원하는 작업을 수행하도록 만듬.
        * */
        return http
                .csrf(csrf -> csrf.disable()) // Stateless 환경에선 CSRF 불필요
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 생성 X
//                =================================================================
                // URL별 입장 규칙
                .authorizeHttpRequests(auth -> auth
                        // 여기서 1차 인가 관련 방호벽
                        // /api/user/ 하위에 endpoint 중에 admin / user 권한 별로 접근하기 위해서는
                        // 메서드 레벨에서 2차 방호벽 구축
                        .requestMatchers("/api/auth/**").permitAll() // 인증 없이 허용
                        .requestMatchers("/api/users/**").hasAnyAuthority( "ROLE_USER")
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()) // 나머지는 인증 필요
//                ========================================================================
                /*
                * addFilterBefore
                * HttpSecurity 설정 내에서 사용되며, Spring Security의 기존 필터 체인에 사용자 정의 필터를 특정 필터 앞에 추가할 때 사용
                * */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // "UsernamePasswordAuthenticationFilter 앞에 jwtAuthenticationFilter를 세워줘"
                /*
                * exceptionHandling
                * 설정 내에서 Spring Security가 보안 관련 예외를 처리하는 방식을 지정
                * */
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint) // 인증되지 않은 사용자가 보호된 리소스 접근시 처리 방식 정의
                        .accessDeniedHandler(accessDeniedHandler)) // 인증은 되었지만 인가가 허용되지 않는 사용자 처리 방식 정의
                .build();
    }
}
