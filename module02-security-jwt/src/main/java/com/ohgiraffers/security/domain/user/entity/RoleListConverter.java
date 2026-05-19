package com.ohgiraffers.security.domain.user.entity;

import com.ohgiraffers.security.domain.user.model.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Converter
public class RoleListConverter implements AttributeConverter<List<Role>, String> {

    // DB 컬럼에는 여러 권한을 하나의 문자열로 저장하므로, 각 권한을 구분할 구분자를 정의한다.
    // 예: ROLE_USER,ROLE_ADMIN
    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<Role> roles) {
        // 엔티티의 roles 값이 없으면 DB에는 빈 문자열로 저장한다.
        if (roles == null || roles.isEmpty()) {
            return "";
        }

        // List<Role>을 DB에 저장 가능한 문자열로 변환한다.
        // 예: [ROLE_USER, ROLE_ADMIN] -> "ROLE_USER,ROLE_ADMIN"
        return roles.stream()
                // null 권한 값이 섞여 있어도 저장 문자열에 포함되지 않도록 제거한다.
                .filter(Objects::nonNull)
                // enum 값은 name()으로 문자열화한다. 예: Role.ROLE_USER -> "ROLE_USER"
                .map(Role::name)
                // 각 권한 문자열을 쉼표로 연결해서 단일 컬럼에 저장할 문자열을 만든다.
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<Role> convertToEntityAttribute(String roles) {
        // DB 컬럼 값이 없으면 엔티티에서는 빈 권한 목록으로 복원한다.
        if (roles == null || roles.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // DB 문자열을 엔티티에서 사용할 List<Role>로 변환한다.
        // 예: "ROLE_USER,ROLE_ADMIN" -> [ROLE_USER, ROLE_ADMIN]
        return Arrays.stream(roles.split(DELIMITER))
                // 쉼표 주변 공백을 제거한다. 예: " ROLE_USER " -> "ROLE_USER"
                .map(String::trim)
                // 연속 쉼표 등으로 생길 수 있는 빈 문자열은 제외한다.
                .filter(role -> !role.isEmpty())
                // 문자열을 Role enum 값으로 복원한다. 예: "ROLE_USER" -> Role.ROLE_USER
                .map(Role::valueOf)
                // 복원된 Role들을 List<Role>로 모은다.
                .collect(Collectors.toList());
    }
}
