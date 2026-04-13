package com.wanted.securitysession.domain.user.model.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginUserDTO {

    private int userCode;
    private String userId;
    private String userName;
    private String password;
    private String userRole;
    private int loginFailCount;
    private boolean isAccountLocked;


    public LoginUserDTO() {
    }

    public LoginUserDTO(int userCode, String userId, String userName, String password, String userRole, int loginFailCount, boolean isAccountLocked) {
        this.userCode = userCode;
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.userRole = userRole;
        this.loginFailCount = loginFailCount;
        this.isAccountLocked = isAccountLocked;
    }

    public List<String> getRole(){
        if(this.userRole != null && this.userRole.length()>0){
            return Arrays.asList(this.userRole.split(","));
        }
        return new ArrayList<>();
    }



    public int getUserCode() {
        return userCode;
    }

    public void setUserCode(int userCode) {
        this.userCode = userCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public int getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(int loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public boolean isAccountLocked() {
        return isAccountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        isAccountLocked = accountLocked;
    }

    @Override
    public String toString() {
        return "LoginUserDTO{" +
                "userCode=" + userCode +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", userRole=" + userRole +
                ", loginFailCount=" + loginFailCount +
                ", isAccountLocked=" + isAccountLocked +
                '}';
    }
}
