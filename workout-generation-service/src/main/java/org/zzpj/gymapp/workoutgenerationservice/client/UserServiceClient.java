package org.zzpj.gymapp.workoutgenerationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.zzpj.gymapp.workoutgenerationservice.dto.UserProfileDTO;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

@FeignClient(value = "user-service", path = "/profile")
public interface UserServiceClient {
    @GetMapping("/{userId}")
    UserProfileDTO getUserProfile(@RequestHeader(name = AUTHORIZATION) String authHeader, @PathVariable Long userId);
}
