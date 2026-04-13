package com.wanted.securitysession.global;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.wanted.securitysession")
@EntityScan(basePackages = "com.wanted.securitysession")
@Configuration
public class JpaConfig {
}
