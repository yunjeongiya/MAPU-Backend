package com.mapu.global.jwt.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class JwtUserDto implements AuthenticatedPrincipal {
    private String role;
    private String name;

    public Map<String, Object> getAttributes() {
        return null;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add((GrantedAuthority) () -> role);
        return collection;
    }

    @Builder
    public JwtUserDto(String role, String name) {
        this.role = role;
        this.name = name;
    }
}