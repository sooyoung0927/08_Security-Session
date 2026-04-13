package com.wanted.securitysession.domain.user.model.dao;


import com.wanted.securitysession.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // 사용자 ID로 사용자 찾기
    Optional<User> findByUserId(String userId);
    
    // 사용자 ID가 이미 존재하는지 확인 (중복 ID 체크)
    boolean existsByUserId(String userId);
}
