package com.wanted.securitysession.domain.user.model.dao;

import com.wanted.securitysession.domain.user.model.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findByUserId(String userId);

    List<LoginLog> findAllByOrderByLoginTimeDesc();
}
