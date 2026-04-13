package com.wanted.securitysession.domain.user.controller;

import com.wanted.securitysession.domain.user.model.dao.LoginLogRepository;
import com.wanted.securitysession.domain.user.model.entity.LoginLog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final LoginLogRepository loginLogRepository;
    private final SessionRegistry sessionRegistry;

    /**
     * 관리자 로그인 로그 페이지를 조회하기 위한 메서드이다.
     *
     * 처리 내용
     * 1. 로그인 로그 전체 조회
     * 2. 현재 활성 사용자 수 계산
     * 3. 관리자 페이지(admin/admin)로 데이터 전달
     *
     * 활성 사용자 수 계산 방식
     * - SessionRegistry의 전체 principal 수를 단순히 세는 것이 아니라
     * - 각 principal이 실제로 만료되지 않은 세션을 가지고 있는지 확인한 뒤 계산한다.
     *
     * @param mv 화면에 전달할 데이터를 담는 객체
     * @return 관리자 페이지와 모델 데이터
     * */
    @GetMapping("/admin/logs")
    public ModelAndView getLoginLogs(ModelAndView mv) {

        List<LoginLog> logs = loginLogRepository.findAllByOrderByLoginTimeDesc();

        // 현재 활성 사용자 수 계산
        // getAllPrincipals()는 SessionRegistry가 알고 있는 사용자 목록을 반환한다.
        // 다만 단순히 size()를 사용하면 이미 로그아웃했거나 세션이 만료된 사용자가 포함될 수 있으므로
        // 각 사용자(principal)가 실제로 만료되지 않은 세션을 가지고 있는지 확인한 뒤 카운트한다.
        int activeUserCount = 0;

        for (Object principal : sessionRegistry.getAllPrincipals()) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);

            // false 옵션은 만료된 세션을 제외한다.
            // 즉 실제로 살아있는 세션이 하나라도 있는 사용자만 활성 사용자로 간주한다.
            if (!sessions.isEmpty()) {
                activeUserCount++;
            }
        }

        // 화면에 전달할 데이터 저장
        mv.addObject("logs", logs);
        mv.addObject("activeUsers", activeUserCount);

        // 이동할 View 지정
        mv.setViewName("admin/admin");

        return mv;
    }
}
