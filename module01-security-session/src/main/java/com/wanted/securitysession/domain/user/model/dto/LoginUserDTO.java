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

    // id,pw 전용 dto가 아니다
    // id,pw는 Security Config에서 담당을 시키고
    // 해당 클래스는 로그인 시 Session에 담아둘 정보를 기입한다

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

    // 문자열로 사용자의 권한을 처리하지만
    // 다중 권한을 위해 여러 권한을 insert 하면
    // USER, ADMIN 이렇게 DB에 들어갈 수 있게 세팅을 해두었다
    // 따라서 해당 메서드는 쉼표를 기준으로 두 문자열을 분리하는 메서드이다
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
