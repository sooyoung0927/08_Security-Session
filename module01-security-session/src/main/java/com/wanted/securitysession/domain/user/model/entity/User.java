package com.wanted.securitysession.domain.user.model.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "TBL_USER",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_id", columnNames = "USER_ID")
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO")
    private Integer userCode;

    @Column(name = "USER_ID", nullable = false, length = 30)
    private String userId;

    @Column(name = "USER_NAME", length = 30)
    private String userName;

    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;

    @Column(name = "USER_ROLE", nullable = false, length = 50)
    private String userRole;

    @Column(name = "LOGIN_FAIL_COUNT", nullable = false)
    private int loginFailCount = 0;

    @Column(name = "IS_ACCOUNT_LOCKED", nullable = false)
    private boolean isAccountLocked = false;

    public User() {
    }

    // Builder methods for chaining
    public User userId(String userId) {
        this.userId = userId;
        return this;
    }

    public User userName(String userName) {
        this.userName = userName;
        return this;
    }

    public User password(String password) {
        this.password = password;
        return this;
    }

    public User userRole(String userRole) {
        this.userRole = userRole;
        return this;
    }

    public User loginFailCount(int loginFailCount) {
        this.loginFailCount = loginFailCount;
        return this;
    }

    public User isAccountLocked(boolean isAccountLocked) {
        this.isAccountLocked = isAccountLocked;
        return this;
    }

    // Getters only, no setters

    public Integer getUserCode() {
        return userCode;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getUserRole() {
        return userRole;
    }

    public int getLoginFailCount() {
        return loginFailCount;
    }

    public boolean isAccountLocked() {
        return isAccountLocked;
    }

    @Override
    public String toString() {
        return "User{" +
                "userCode=" + userCode +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", userRole='" + userRole + '\'' +
                ", loginFailCount=" + loginFailCount +
                ", isAccountLocked=" + isAccountLocked +
                '}';
    }
}
