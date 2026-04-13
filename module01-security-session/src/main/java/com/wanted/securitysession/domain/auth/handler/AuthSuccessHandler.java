package com.wanted.securitysession.domain.auth.handler;

import com.wanted.securitysession.domain.user.model.dao.LoginLogRepository;
import com.wanted.securitysession.domain.user.model.entity.LoginLog;
import com.wanted.securitysession.domain.user.model.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;


/**
 * 사용자의 로그인 성공 시
 * 성공 요청을 커스텀 하기 위한 핸들러이다.
 *
 * 로그인 성공 시 실패 횟수를 초기화하여
 * 4번 실패 후 성공하면 카운트를 0으로 리셋한다.
 *
 * 추가 기능
 * 1. 로그인 성공 시 실패 횟수 초기화
 * 2. 로그인 성공 로그 저장
 * */
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberService memberService;
    private final LoginLogRepository loginLogRepository;

    public AuthSuccessHandler(MemberService memberService, LoginLogRepository loginLogRepository) {
        this.memberService = memberService;
        this.loginLogRepository = loginLogRepository;
    }

    /**
     * 사용자의 성공적인 로그인을 처리하기 위한 핸들러이다.
     * 로그인 성공 시 해당 사용자의 실패 횟수를 0으로 초기화한다.
     *
     * @param request 사용자 요청 개체
     * @param response 서버 응답값
     * @param authentication 인증된 사용자 정보
     * */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 인증된 사용자의 아이디(username) 가져오기
        String username = authentication.getName();

        // 로그인 성공 시 실패 횟수 초기화
        memberService.resetLoginFailCount(username);

        // 로그인 성공 로그 저장
        // 성공 시에는 인증이 완료된 객체(authentication)에서 username을 가져와 기록한다.
        loginLogRepository.save(
                new LoginLog(
                        username,
                        LocalDateTime.now(),
                        true,
                        request.getRemoteAddr()
                )
        );

        // 기본 로그인 성공 처리 (defaultSuccessUrl 또는 savedRequestUrl로 리다이렉트)
        super.onAuthenticationSuccess(request, response, authentication);
    }
}