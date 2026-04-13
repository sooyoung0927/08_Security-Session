package com.wanted.securitysession.domain.user.model.service;

import com.wanted.securitysession.domain.user.model.dao.UserRepository;
import com.wanted.securitysession.domain.user.model.dto.LoginUserDTO;
import com.wanted.securitysession.domain.user.model.dto.SignupDTO;
import com.wanted.securitysession.domain.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final ModelMapper modelMapper;


    @Transactional
    public Integer regist(SignupDTO signupDTO) {
        // 중복 아이디 체크
        if (userRepository.existsByUserId(signupDTO.getUserId())) {
            return null; // 중복된 아이디가 존재함을 null로 표시
        }
        
        try {
            // DTO를 엔티티로 변환 (ModelMapper 사용 후 Builder 패턴으로 password 설정)
            User user = modelMapper.map(signupDTO, User.class)
                    .password(encoder.encode(signupDTO.getUserPass()))
                    .userRole(signupDTO.getRole());
            
            // 저장 후 생성된 사용자 코드 반환
            User savedUser = userRepository.save(user);
            return savedUser.getUserCode();

        } catch (Exception e) {
            e.printStackTrace();
            return 0; // 서버 오류를 0으로 표시
        }
    }

    /**
     * 사용자의 id를 입력받아 DB에서 회원을 조회하는 메서드
     * @param username : 사용자 id
     * @return LoginUserDTO : LoginUserDTO 사용자 개체
     */
    public LoginUserDTO findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUserId(username);
        
        // 엔티티를 DTO로 변환 (ModelMapper 사용)
        return userOptional.map(user -> modelMapper.map(user, LoginUserDTO.class)).orElse(null);
    }

    /**
     * 로그인 실패 횟수를 증가시키고, 5회 이상이면 계정을 Lock 하는 메서드
     * @param username : 사용자 id
     */
    @Transactional
    public void incrementLoginFailCount(String username) {
        Optional<User> userOptional = userRepository.findByUserId(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            int currentFailCount = user.getLoginFailCount();
            int newFailCount = currentFailCount + 1;
            
            // 5회 이상 실패하면 계정 Lock
            if (newFailCount >= 5) {
                user.loginFailCount(newFailCount)
                    .isAccountLocked(true);
            } else {
                user.loginFailCount(newFailCount);
            }
        }
    }

    /**
     * 로그인 성공 시 실패 횟수를 초기화하는 메서드
     * @param username : 사용자 id
     */
    @Transactional
    public void resetLoginFailCount(String username) {
        Optional<User> userOptional = userRepository.findByUserId(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.loginFailCount(0);
        }
    }
}
