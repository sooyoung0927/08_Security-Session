package com.ohgiraffers.security.auth.service;


import com.ohgiraffers.security.auth.dto.LoginRequestDto;
import com.ohgiraffers.security.auth.dto.TokenResponseDTO;
import com.ohgiraffers.security.auth.jwt.JwtTokenProvider;
import com.ohgiraffers.security.auth.token.entity.RefreshToken;
import com.ohgiraffers.security.auth.token.repository.RefreshTokenRepository;
import com.ohgiraffers.security.domain.user.dto.SignupRequestDto;
import com.ohgiraffers.security.domain.user.dto.UserResponseDto;
import com.ohgiraffers.security.domain.user.entity.User;
import com.ohgiraffers.security.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder; // SecurityConfig 에서 빈으로 등록해줌 -> PasswordEncoder는 인터페이스라 구현체가 없음
    private final UserRepository userRepository;// SecurityConfig 에서 빈으로 등록해줌 -> 애도 인터페이스라 구현체가 없음
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * 🎯 로그인 처리: AccessToken만 클라이언트로 응답,
     *    RefreshToken은 서버(In-Memory)에 저장
     */
    @Transactional
    public TokenResponseDTO login(LoginRequestDto loginRequestDto) {
        // 1. 사용자 인증 시도
        // AuthenticationManager.authenticate()는 인증 실패 시 AuthenticationException을 발생시킨다
        // AuthenticationManager는 "아이디/비밀번호 맞는지 확인하는 심사관"입니다.
        // authenticate() : 입력된 미인증 객체를 바탕으로 인증 절차를 수행하는 메서드로 인증에 성공하면 Authentication 객체를 반환
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())
//                "아이디는 이거고 비밀번호는 이거야"라는 미인증 상태의 정보 묶음을 만듭니다. 아직 확인이 안 된 상태입니다.
        );
        /*
        * authenticationManager.authenticate(...) — 이
        * 심사관에게 위 묶음을 넘기면 내부적으로 아래 과정이 자동으로 실행됩니다:

           1. CustomUserDetailsService.loadUserByUsername("hong") 호출
           2. UserService.findByUsername("hong") → UserRepository에서 DB 조회
           3. 사용자가 없으면 UsernameNotFoundException 예외 발생 → 로그인 실패
           4. 사용자가 있으면 passwordEncoder.matches("입력한비밀번호", "DB의암호화된비밀번호") 실행
           5. 비밀번호가 틀리면 BadCredentialsException 예외 발생 → 로그인 실패
           6. 모두 통과하면 인증된 Authentication 객체 반환

            이 6단계가 전부 authenticate() 한 줄 안에서 자동으로 처리
        * */



        // 인증 성공 후 토큰 두 개를 만듭니다.
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        // 사용자 정보가 담긴 authentication를 AccessToke 만들 때 넘겨서 사용자 정보를 AccessToke 안에 새김
        String newRefreshTokenValue = jwtTokenProvider.createRefreshToken(); // 새 Refresh Token 생성
        System.out.println(authentication.getName());

        // 해당 사용자의 기존 Refresh Token이 있다면 삭제 (선택적: 하나의 세션만 허용하는 경우)
        // refreshTokenRepository.deleteByUsername(authentication.getName());
        refreshTokenRepository.findByUsername(authentication.getName()).ifPresent(refreshTokenRepository::delete);
        refreshTokenRepository.findAll().forEach(System.out::println);

        // 새 Refresh Token을 DB에 저장
        RefreshToken newRefreshToken = new RefreshToken(
                authentication.getName(), // 사용자 이름
                newRefreshTokenValue,     // 생성된 Refresh Token 값
                Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenValidityMilliseconds()) // 만료 시간 설정
        );

        refreshTokenRepository.save(newRefreshToken);

        return new TokenResponseDTO(accessToken, newRefreshTokenValue);
        // 두 토큰을 묶어서 반환합니다. 컨트롤러가 이걸 받아서 JSON으로 변환해 클라이언트에게 응답합니다.
        // 로그인 성공 시 프론트에게 전달해줄 값
    }


    public UserResponseDto register(SignupRequestDto request) {

//        ==================프로젝트의 비즈닉스 정책==================
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
//        ========================================================

//        =================사용자 정보를 user에 저장=================
        /*comment
        *  new 키워드를 사용하는 게 아닌
        *  builder 메서드 혹은 팩토리 메서드릃 활용하는 것이 추천되는 구조이다 */
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRoles())
                .build();

        User saveUser = userRepository.save(user);
//        =========================================================

        return new UserResponseDto(saveUser);
    }



}