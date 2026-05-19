package com.ohgiraffers.security.auth.token.service;

import com.ohgiraffers.security.auth.jwt.JwtTokenProvider;
import com.ohgiraffers.security.auth.token.entity.RefreshToken;
import com.ohgiraffers.security.auth.token.repository.RefreshTokenRepository;
import com.ohgiraffers.security.domain.user.entity.User;
import com.ohgiraffers.security.domain.user.repository.UserRepository;
import com.ohgiraffers.security.exception.ExpiredJwtCustomException;
import com.ohgiraffers.security.exception.InvalidJwtCustomException;
import com.ohgiraffers.security.exception.InvalidRefreshTokenException;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RefreshService {


    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshService(JwtTokenProvider jwtTokenProvider, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String refreshAccessToken(String oldRefreshTokenValue) {
        // 1. 이전 Refresh Token의 JWT 자체 유효성 검사 (구조, 서명, JWT 만료 시간)
        try {
            jwtTokenProvider.validateToken(oldRefreshTokenValue); // 이 메서드는 JWT가 유효하지 않거나 만료되면 예외를 던짐
        } catch (ExpiredJwtCustomException e) {
            // Refresh Token (JWT) 자체가 만료된 경우, 이는 유효하지 않은 리프레시 시도임
            // DB에서도 해당 토큰을 찾아 삭제하는 것이 좋음
            refreshTokenRepository.findByToken(oldRefreshTokenValue).ifPresent(refreshTokenRepository::delete);
            throw new InvalidRefreshTokenException("Refresh token (JWT) has expired.", e);
        } catch (InvalidJwtCustomException e) {
            // Refresh Token (JWT) 자체가 잘못된 형식이나 서명을 가진 경우
            throw new InvalidRefreshTokenException("Refresh token (JWT) is invalid.", e);
        }

        // 2. DB에서 Refresh Token 조회
        RefreshToken oldRefreshTokenEntity = refreshTokenRepository.findByToken(oldRefreshTokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found in DB."));

        // 3. DB에서 가져온 토큰이 (우리 시스템상의) 만료 시간을 넘었는지 확인
        if (oldRefreshTokenEntity.isExpired()) {
            refreshTokenRepository.delete(oldRefreshTokenEntity); // 만료된 토큰은 DB에서 정리
            throw new InvalidRefreshTokenException("Refresh token has expired (DB).");
        }

        // 4. Refresh Token과 연결된 사용자 정보 조회
        String username = oldRefreshTokenEntity.getUsername();
        User user = userRepository.findByUsername(username) // 실제 User 엔티티와 UserRepository 사용
                .orElseThrow(() -> new UsernameNotFoundException("User not found for refresh token: " + username));

        // 5. 사용자에 대한 Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(user.getUsername(), "", user.getAuthorities()),
                null,
                user.getAuthorities()
        );

        // 6. 새 Access Token 생성
        return jwtTokenProvider.createAccessToken(authentication);
    }
}
