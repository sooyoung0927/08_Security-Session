package com.wanted.securitysession.domain.user.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_LOGIN_LOG")
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "LOGIN_TIME", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "SUCCESS", nullable = false)
    private boolean success;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    public LoginLog() {}

    public LoginLog(String userId, LocalDateTime loginTime, boolean success, String ipAddress) {
        this.userId = userId;
        this.loginTime = loginTime;
        this.success = success;
        this.ipAddress = ipAddress;
    }

    // Getters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public boolean isSuccess() { return success; }
    public String getIpAddress() { return ipAddress; }
}
