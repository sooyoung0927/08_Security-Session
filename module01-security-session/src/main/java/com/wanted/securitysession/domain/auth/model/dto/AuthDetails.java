package com.wanted.securitysession.domain.auth.model.dto;

import com.wanted.securitysession.domain.user.model.dto.LoginUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * UserDetailService에서 사용자의 이름을 기준으로 조회한 결과가
 * 반환되는 사용자 타입으로 해당 개체에 조회된 사용자의 정보가 담겨서 session에 저장할 수 있다.
 * */
public class AuthDetails implements UserDetails {
    /*comment
    *  UserDetail 상속 받아 로그인 시 Session에 담아둘 정보 커스터마이징*/

    private LoginUserDTO loginUserDTO;

    // session에 담을 용도로 사용하는 커스텀 DTO
    public AuthDetails() {
    }

    public AuthDetails(LoginUserDTO loginUserDTO) {
        this.loginUserDTO = loginUserDTO;
    }

    public LoginUserDTO getLoginUserDTO() {
        return loginUserDTO;
    }

    public void setLoginUserDTO(LoginUserDTO loginUserDTO) {
        this.loginUserDTO = loginUserDTO;
    }

    /**
     * 권한 정보를 반환하는 메서드이다.
     * UsernamePasswordAuthenticationToken에 사용자의 권한 정보를 넣을 때 사용된다.
     * */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        loginUserDTO.getRole().forEach(role -> authorities.add(() -> role));

        return authorities;
    }

    /**
     * 사용자의 비밀번호를 반환하는 메서드이다.
     * UsernamePasswordAuthenticationToken과 사용자의 비밀번호를 비교할 때 사용된다.
     * */
    @Override
    public String getPassword() {
        return loginUserDTO.getPassword();
    }

    /**
     * 사용자의 아이디를 반환하는 메서드이다.
     * UsernamePasswordAuthenticationToken과 사용자의 아이디를 비교할 때 사용된다.
     * */
    // username은 ID이다 잊지 말자
    @Override
    public String getUsername() {
        return loginUserDTO.getUserId();
    }

    /**
     * 계정 만료 여부를 표현하는 메서드로
     * false이면 해당 계정을 사용할 수 없다.
     * */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 잠겨있는 계정을 확인하는 메서드로
     * false이면 해당 계정을 사용할 수 없다.
     *
     * 비밀번호 반복 실패로 일시적인 계정 lock의 경우
     * 혹은 오랜 기간 비 접속으로 휴면 처리
     * */
    @Override
    public boolean isAccountNonLocked() {
        return !loginUserDTO.isAccountLocked();
    }

    /**
     * 탈퇴 계정 여부를 표현하는 메서드
     * false면 해당 계정을 사용할 수 없다.
     *
     * 보통 데이터 삭제는 즉시 하는 것이 아닌 일정 기간 보관 후 삭제한다.
     * */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 비활성화 여부로 사용자가 사용할 수 없는 상태
     * false이면 계정을 사용할 수 없다.
     *
     * 삭제 처리 같은 경우
     * */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * equals() 메서드는 두 객체가 "논리적으로 같은 사용자"인지 판단하기 위해 사용된다.
     *
     * 왜 필요한가?
     * Spring Security의 SessionRegistry는 내부적으로 Map 형태로 사용자를 관리한다.
     * 이때 key로 principal(AuthDetails 객체)이 사용되며,
     * 사용자 비교 시 equals()와 hashCode()를 사용한다.
     *
     * 문제 상황:
     * - 같은 사용자라도 로그인할 때마다 새로운 AuthDetails 객체가 생성된다.
     * - equals()를 오버라이딩하지 않으면 기본 Object.equals()가 사용되는데,
     *   이는 "주소값 비교"이기 때문에 서로 다른 객체로 인식된다.
     *
     * 결과:
     * - 같은 사용자라도 여러 명으로 인식됨
     * - SessionRegistry에서 활성 사용자 수가 실제보다 많게 나올 수 있음
     *
     * 해결 방법:
     * - 사용자 식별 기준이 되는 값(여기서는 userId)을 기준으로 비교해야 한다.
     * */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthDetails that)) return false;

        // userId 기준으로 동일 사용자 판단
        return Objects.equals(this.getUsername(), that.getUsername());
    }

    /**
     * hashCode()는 equals()와 반드시 같이 오버라이딩해야 한다.
     *
     * 왜 필요한가?
     * - HashMap, HashSet 같은 자료구조에서 객체를 빠르게 찾기 위해 사용된다.
     * - SessionRegistry도 내부적으로 Map 구조를 사용하기 때문에 hashCode가 중요하다.
     *
     * 규칙:
     * - equals()에서 사용한 기준과 동일한 기준으로 hashCode를 생성해야 한다.
     *
     * 잘못된 경우:
     * - equals는 userId 기준인데 hashCode는 DTO 전체 기준이면
     *   같은 사용자라도 다른 hashCode가 나올 수 있음 → 버그 발생
     *
     * 따라서:
     * - userId 기준으로 hashCode 생성
     * */
    @Override
    public int hashCode() {
        return Objects.hash(this.getUsername());
    }
}