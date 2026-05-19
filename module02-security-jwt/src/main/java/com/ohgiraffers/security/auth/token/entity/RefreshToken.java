package com.ohgiraffers.security.auth.token.entity;

import jakarta.persistence.*;

import java.time.Instant;

/*******************************************
 📖 개념: RefreshToken 저장소가 필요한 이유
 ********************************************/

/*
✅ 왜 필요한가?
- AccessToken은 일반적으로 짧은 수명(15분~1시간)을 가짐 → 유출 시 피해 최소화
- 사용자가 계속 로그인 상태를 유지하려면, 만료된 AccessToken을 RefreshToken으로 갱신해야 함
- RefreshToken은 상대적으로 긴 수명을 가지므로 반드시 안전하게 저장되어야 함

✅ 언제 사용되는가?
- 사용자가 AccessToken이 만료된 상태에서 자동 로그인하거나
- 클라이언트가 백그라운드에서 갱신 요청을 보내는 경우

//======================================================
✅ 실무 고려사항
- RefreshToken을 DB 또는 Redis에 저장하여 상태 관리 (탈취/만료 판단)
- 유저마다 고유하게 1개만 저장하거나, IP·Device 정보와 함께 복수 관리 가능
- 블랙리스트 또는 재사용 방지를 위한 관리 로직 필요
//======================================================

✅ token 관리 전략은 다양한 방식이 있지만 이번에는 아래의 전략을 다룬다.
- 데이터베이스(DB)를 이용한 저장 및 관리 전략 💾
- 사용자 인증시 토큰을 db에 저장하고 관리하는 방식

[로직]
1. 로그인 성공 시, 액세스 토큰과 함께 리프레시 토큰을 생성
2. 생성된 리프레시 토큰(또는 토큰의 해시값/고유 식별자)을 데이터베이스 테이블(예: refresh_tokens)에 사용자 ID, 만료 시간, 발급 시간, 사용 여부/폐기 여부 등과 함께 저장.
3. 클라이언트에게는 실제 리프레시 토큰 문자열을 전달.
4. 클라이언트가 액세스 토큰 재발급을 요청하며 리프레시 토큰을 보내오면, 서버는 DB에서 해당 토큰을 조회
5. DB에 토큰이 존재하고, 유효 기간이 남았으며, 폐기되지 않았는지 등을 검증한 후 새 액세스 토큰을 발급 (선택적으로 새 리프레시 토큰도 발급 - 아래 "토큰 회전" 참조)
*/
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 사용자 식별자 (예: User 엔티티의 username 또는 ID)

    @Column(nullable = false, length = 1024) // 토큰 길이를 고려하여 충분한 길이 설정
    private String token;

    @Column(nullable = false)
    // UTC 기준으로 시을 정의함
    private Instant expiryDate;

    public RefreshToken() {
    }

    public RefreshToken(String username, String token, Instant expiryDate) {
        this.username = username;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}

/*
🚀 심화
- 대규모 서비스에서는 RefreshToken을 DB보다 Redis에 저장하여 속도 향상
  - Redis: TTL 기능으로 자동 만료 관리 가능
  - Key 구성 예: `refresh:userId` → value: token

- 보안 강화를 위한 확장 전략:
  - User-Agent, IP, deviceId 등 추가 메타데이터와 함께 저장
  - 같은 유저라도 다른 디바이스에선 다른 RefreshToken을 발급

- 토큰 블랙리스트 정책을 Redis Set으로 구현하여 재사용 방지 가능
* */
