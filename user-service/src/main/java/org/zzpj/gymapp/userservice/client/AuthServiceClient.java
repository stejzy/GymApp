package org.zzpj.gymapp.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@FeignClient(value = "auth-service", path = "/users")
public interface AuthServiceClient {

    @GetMapping("/{userId}/roles")
    Set<String> getUserRoles(@PathVariable Long userId, 
                           @RequestHeader("Authorization") String authHeader);


    @GetMapping("/role/{role}")
    List<Long> getUserIdsByRole(@PathVariable String role,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "20") int size,
                               @RequestHeader("Authorization") String authHeader);
}