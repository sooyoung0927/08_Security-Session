package com.ohgiraffers.security.domain.user.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/test")
    public ResponseEntity<String> test(@AuthenticationPrincipal UserDetails user) {
        if(user != null) {
            String username = user.getUsername();
            return ResponseEntity.status(HttpStatus.OK).body("hello user : " + username);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}