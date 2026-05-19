package com.ohgiraffers.security.domain.user.service;

import com.ohgiraffers.security.domain.user.entity.User;
import com.ohgiraffers.security.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
