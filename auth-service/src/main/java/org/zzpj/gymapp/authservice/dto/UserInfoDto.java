package org.zzpj.gymapp.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.zzpj.gymapp.authservice.entity.Role;

import java.util.Collection;
import java.util.Set;

@Getter
@AllArgsConstructor
public class UserInfoDto {
    private Long id;
    private String username;
    private Set<Role> roles;
    private Collection<?> authorities;
} 